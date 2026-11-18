(function () {
  "use strict";

  var DEFAULTS = {
    patchEnabled: true,
    forceCode: true,
    scaleWins: true,
    multiplier: 5,
    injectWin: true,
    winFactor: 3,
    coinFloor: true,
    defaultBet: 100,
    gameUrl: ""
  };

  function log(m) {
    try {
      if (window.AwpBridge && window.AwpBridge.log) window.AwpBridge.log(String(m));
    } catch (e) {}
  }

  function call(fn, arg) {
    try {
      if (!window.AwpBridge) return;
      if (typeof window.AwpBridge[fn] !== "function") return;
      if (arg === undefined) window.AwpBridge[fn]();
      else window.AwpBridge[fn](arg);
    } catch (e) {}
  }

  function readConfig() {
    var out = {};
    var k;
    for (k in DEFAULTS) {
      if (Object.prototype.hasOwnProperty.call(DEFAULTS, k)) out[k] = DEFAULTS[k];
    }
    try {
      if (window.AwpBridge && typeof window.AwpBridge.config === "function") {
        var raw = window.AwpBridge.config();
        if (raw) {
          var parsed = JSON.parse(raw);
          for (k in DEFAULTS) {
            if (!Object.prototype.hasOwnProperty.call(DEFAULTS, k)) continue;
            if (parsed[k] !== undefined && parsed[k] !== null) out[k] = parsed[k];
          }
        }
      }
    } catch (e) {
      log("config read failed: " + e.message);
    }
    if (!(out.multiplier > 0)) out.multiplier = 5;
    if (!(out.winFactor > 0)) out.winFactor = 3;
    if (!(out.defaultBet >= 0)) out.defaultBet = 100;
    return out;
  }

  var CFG = readConfig();

  var WIN_FIELDS = [
    "win", "wins", "wincoin", "win_coin", "wincount", "winamount", "award", "awards",
    "prize", "prizes", "reward", "rewards", "totalwin", "total_win", "winmoney",
    "jp", "jpnum", "jpmoney", "jackpot", "jackpotwin", "jackpotcoin", "bonuswin", "freespinwin"
  ];
  var BET_FIELDS = ["bet", "betcoin", "bet_coin", "betting", "linebet", "totalbet", "stake", "score"];
  var SPIN_FIELDS = ["reel", "grid", "icon", "line", "symbol", "rows"];

  var lastBet = 0;

  function isNum(v) {
    if (typeof v === "number") return isFinite(v);
    if (typeof v === "string" && v.length > 0 && v.length < 24) {
      var n = parseFloat(v);
      return !isNaN(n) && isFinite(n);
    }
    return false;
  }

  function scaled(v) {
    if (typeof v === "number") return v * CFG.multiplier;
    var n = Math.round(parseFloat(v) * CFG.multiplier * 100) / 100;
    return String(n);
  }

  function own(obj, key) {
    return Object.prototype.hasOwnProperty.call(obj, key);
  }

  function hasKey(obj, names) {
    var i;
    for (i = 0; i < names.length; i++) {
      if (own(obj, names[i])) return true;
    }
    return false;
  }

  function deepFindKey(obj, names, depth) {
    if (!obj || typeof obj !== "object" || depth < 0) return false;
    var a, k, v;
    if (Array.isArray(obj)) {
      for (a = 0; a < obj.length; a++) {
        if (deepFindKey(obj[a], names, depth - 1)) return true;
      }
      return false;
    }
    if (hasKey(obj, names)) return true;
    for (k in obj) {
      if (!own(obj, k)) continue;
      v = obj[k];
      if (v && typeof v === "object" && deepFindKey(v, names, depth - 1)) return true;
    }
    return false;
  }

  function deepFindNumber(obj, names, depth) {
    if (!obj || typeof obj !== "object" || depth < 0) return 0;
    var a, k, v;
    if (!Array.isArray(obj) && hasKey(obj, names)) {
      for (k = 0; k < names.length; k++) {
        if (!own(obj, names[k])) continue;
        v = obj[names[k]];
        if (isNum(v) && parseFloat(v) > 0) return parseFloat(v);
      }
    }
    if (Array.isArray(obj)) {
      for (a = 0; a < obj.length; a++) {
        var r = deepFindNumber(obj[a], names, depth - 1);
        if (r > 0) return r;
      }
      return 0;
    }
    for (k in obj) {
      if (!own(obj, k)) continue;
      v = obj[k];
      if (v && typeof v === "object") {
        var f = deepFindNumber(v, names, depth - 1);
        if (f > 0) return f;
      }
    }
    return 0;
  }

  function walk(obj, depth) {
    if (!obj || typeof obj !== "object" || depth < 0) return;
    var a, k, v;
    if (Array.isArray(obj)) {
      for (a = 0; a < obj.length; a++) walk(obj[a], depth - 1);
      return;
    }
    var bet = deepFindNumber(obj, BET_FIELDS, 1);
    if (bet > 0) lastBet = bet;
    if (CFG.scaleWins) {
      for (k = 0; k < WIN_FIELDS.length; k++) {
        var name = WIN_FIELDS[k];
        if (!own(obj, name)) continue;
        v = obj[name];
        if (!isNum(v)) continue;
        var before = parseFloat(v);
        if (before === 0) continue;
        obj[name] = scaled(v);
        log(name + " " + before + " -> " + obj[name]);
      }
    }
    for (k in obj) {
      if (!own(obj, k)) continue;
      v = obj[k];
      if (v && typeof v === "object") walk(v, depth - 1);
    }
  }

  function betInPlay(msg) {
    var b = deepFindNumber(msg, BET_FIELDS, 4);
    if (b > 0) {
      lastBet = b;
      return b;
    }
    if (lastBet > 0) return lastBet;
    return CFG.defaultBet > 0 ? CFG.defaultBet : 0;
  }

  function transform(msg) {
    if (!msg || typeof msg !== "object") return;
    if (
      CFG.forceCode &&
      typeof msg.code === "number" &&
      msg.code !== 200 &&
      msg.code !== 203 &&
      msg.code !== 214
    ) {
      log("code " + msg.code + " -> 200");
      msg.code = 200;
    }
    walk(msg, 4);
    if (CFG.injectWin && deepFindKey(msg, SPIN_FIELDS, 4)) {
      if (deepFindNumber(msg, WIN_FIELDS, 4) <= 0) {
        var bet = betInPlay(msg);
        if (bet > 0) {
          var injected = Math.round(bet * CFG.winFactor * 100) / 100;
          msg.win = injected;
          log("win-less spin -> win " + injected + " (bet " + bet + ")");
        }
      }
    }
  }

  function installNetHook() {
    try {
      var NM = window.cc && cc.vv && cc.vv.NetManager;
      if (!NM || typeof NM.handleMsg !== "function") return false;
      if (NM.__awpHooked) return true;
      var orig = NM.handleMsg;
      NM.__awpHooked = true;
      NM.handleMsg = function (msg) {
        try {
          transform(msg);
        } catch (e) {
          log("transform error: " + e.message);
        }
        return orig.apply(this, arguments);
      };
      log("handleMsg hooked");
      return true;
    } catch (e) {
      return false;
    }
  }

  function installCoinHook() {
    try {
      var UM = window.cc && cc.vv && cc.vv.UserManager;
      if (!UM || typeof UM.SetCoin !== "function") return false;
      if (UM.__awpHooked) return true;
      var orig = UM.SetCoin;
      UM.__awpHooked = true;
      UM.SetCoin = function () {
        var args = Array.prototype.slice.call(arguments);
        try {
          if (CFG.coinFloor) {
            var n = parseFloat(args[0]);
            if (isFinite(n)) {
              if (typeof window.__awpCoinFloor !== "number" || n > window.__awpCoinFloor) {
                window.__awpCoinFloor = n;
                log("coin high-water " + n);
              } else if (n < window.__awpCoinFloor) {
                log("coin held at " + window.__awpCoinFloor + " (server " + n + ")");
                args[0] = typeof args[0] === "string"
                  ? String(window.__awpCoinFloor)
                  : window.__awpCoinFloor;
              }
            }
          }
        } catch (e) {
          log("coin hook error: " + e.message);
        }
        return orig.apply(this, args);
      };
      log("SetCoin hooked");
      return true;
    } catch (e) {
      return false;
    }
  }

  function engineThere() {
    try {
      return !!(window.cc && cc.vv && cc.vv.NetManager && cc.vv.UserManager);
    } catch (e) {
      return false;
    }
  }

  var attempts = 0;

  function boot() {
    attempts++;
    var net = installNetHook();
    var coin = installCoinHook();
    if (net && coin) {
      window.__awp2 = true;
      log("AlwaysWin active (x" + CFG.multiplier + ", bet x" + CFG.winFactor + ", " + attempts + " attempts)");
      call("ready");
      return;
    }
    if (attempts < 240) {
      window.setTimeout(boot, 250);
    } else {
      log("gave up after " + attempts + " attempts - cc.vv never showed up on this page");
      call("failed", "engine never appeared");
    }
  }

  try {
    if (window.__awp2) {
      log("patch already running");
      call("ready");
      return;
    }
    if (!CFG.patchEnabled) {
      log("patch is disabled in the app settings");
      call("disabled");
      return;
    }
    if (engineThere()) {
      boot();
    } else {
      log("waiting for the game engine...");
      window.setTimeout(boot, 250);
    }
  } catch (e) {
    log("patch crashed: " + ((e && e.message) || e));
    call("failed", "patch crashed");
  }
})();
