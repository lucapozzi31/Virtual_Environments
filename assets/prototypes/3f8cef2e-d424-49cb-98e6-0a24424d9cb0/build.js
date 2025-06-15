function configure3D() {
    
    app = parametric.appearance().diffuseColor(0xFF0000);
    builder.parametric('parametrics/' + instance.uuid + '.ptx', box(width, depth, height).appearance(app));
}

