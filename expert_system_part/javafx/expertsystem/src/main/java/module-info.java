module expertsystem {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive json.simple;

    opens project to javafx.fxml;
    opens project.controllers to javafx.fxml;
    exports project;
    exports project.controllers;
    exports project.inference;
}
