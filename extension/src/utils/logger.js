(function installLogger(root) {
  const prefix = "[Miyad]";
  const logger = {
    debug(...args) {
      if (root.__MIYAD_DEBUG__) console.debug(prefix, ...args);
    },
    info(...args) {
      console.info(prefix, ...args);
    },
    warn(...args) {
      console.warn(prefix, ...args);
    },
    error(...args) {
      console.error(prefix, ...args);
    }
  };
  root.MiyadLogger = logger;
})(globalThis);
