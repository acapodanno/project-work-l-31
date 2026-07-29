// Configurazione Karma custom, necessaria principalmente per aggiungere il
// launcher `ChromeHeadlessCI`: Chrome headless di default fallisce dentro un
// container Docker perché il sandbox di Chrome richiede permessi che il
// container non concede. I flag `--no-sandbox` risolvono il problema.
// `CHROME_BIN` viene impostata dal Dockerfile (stage `test`) puntando al
// binario Chromium installato via apk; in locale, se non impostata, Karma
// userà l'installazione di Chrome/Chromium di sistema.

module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
    ],
    client: {
      jasmine: {},
      clearContext: false,
    },
    jasmineHtmlReporter: {
      suppressAll: true,
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/healthcare-frontend'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' },
        { type: 'lcovonly' },
        { type: 'cobertura' },
      ],
    },
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage'],
      },
    },
    reporters: ['progress', 'kjhtml'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['Chrome'],
    singleRun: false,
    restartOnFileChange: true,
  });
};
