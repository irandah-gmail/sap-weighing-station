This folder is where the Angular production build lands automatically
(see frontend/angular.json -> architect.build.options.outputPath).

Run:
  cd frontend
  npm install
  npm run build

...and the compiled index.html/js/css will appear here, replacing this file.
Spring Boot serves everything in this folder as static web content, so the
JAR becomes a single self-contained deployable artifact.
