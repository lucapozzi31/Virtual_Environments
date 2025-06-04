
var imports = new JavaImporter(java.io, java.lang, java.util, java.awt);

with (imports) {
    function configure3D() {

        modelFile = localFile('/model/gripper.xml');
        var m = builder.createChildModel(modelFile);

    };
}