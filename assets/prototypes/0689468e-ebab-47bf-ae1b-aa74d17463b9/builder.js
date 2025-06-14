// PARAMETERS
var length;
var in1Length;
var in2Length;
var in3Length;
var width;
var height;
var BASELINE_HEIGHT = 958;

//--- profile params ---
var prfD = 14; // profile depth
var prfTh = 4.5; // profile thickness
var prfHg = 50; // profile height
var openHg = 14; // opening of the profile
var internalH = 23.4; // altezza interna profilo

var tappTh = 1.5; // spessore tappeto
var deltaTapp = 12; // differenza tappeto - width
var tappDst = 1.5; // distanza tappeto - base

var cylRadius = 50 / 2.0;

var legWidth = 25;
var legConeHgt = 30;
var legConeRadius = 15;
var legInternalDisp = 150;


var app = parametric.appearance()
        .diffuseColor(0xE6E6E6)
        .emissiveColor(0x000000)
        .specularColor(0x1A1A1A)
        .shininess(0.504);

var appTapp = parametric.appearance()
        .diffuseColor(0xFFFFFF)
        .emissiveColor(0x000000)
        .specularColor(0x7F7F7F)
        .shininess(0.409)
        .ambientColor(0x7F7F7F);

var appTable = parametric.appearance()
        .diffuseColor(0x6666FF)
        .emissiveColor(0x000000)
        .specularColor(0x7F7F7F)
        .shininess(0.409)
        .ambientColor(0x7F7F7F);

var dirName = 'shuttle/' + instance.uuid + '/';




function configure3D() {

    var conv = conveyor('main', length, true, true, false, false, length * 0.25);
    
    var tableBox = box(width, width, 50).appearance(appTable);
    conv
            .createChild('table')
            .parametric(dirName + 'table.ptx', tableBox)
            .translate(-length*0.5 + width*0.5, 0, height);
}

function conveyor(cname, lgt, frontLegs, rearLegs, cylAPresent, cylBPresent, dpos) {
    var cowner = builder.createChild(cname);
    lwMidStructure(cowner, cname, lgt, cylAPresent, cylBPresent);
    if (frontLegs) {
        lwLeg(cowner, 1, lgt / 2.0 - legInternalDisp, width / 2.0 - legWidth / 2.0, cname);
        lwLeg(cowner, 2, lgt / 2.0 - legInternalDisp, -width / 2.0 + legWidth / 2.0, cname);
        crossbar(cowner, 2, +lgt / 2.0 - legInternalDisp, cname);
    }
    if (rearLegs) {
        lwLeg(cowner, 3, -lgt / 2.0 + legInternalDisp, -width / 2.0 + legWidth / 2.0, cname);
        lwLeg(cowner, 4, -lgt / 2.0 + legInternalDisp, width / 2.0 - legWidth / 2.0, cname);
        crossbar(cowner, 1, -lgt / 2.0 + legInternalDisp, cname);
    }
    cowner
            .frame('startFrame', -lgt / 2.0, 0, height, 0, 0, 0)
            .frame('endFrame', +lgt / 2.0, 0, height, 0, 0, 0)
    return cowner;
}

function lwMidStructure(cowner, cname, lgt, cylAPresent, cylBPresent) {

    var strName = cname + '_midStructure';
    var tappetoName = cname + '_tappeto';
    var cylAName = cname + '_cylA';
    var cylBName = cname + '_cylB';

    var structure = extrusion()
            .lineTo(width + 1, 0)
            .lineTo(width + 1, prfHg)
            .lineTo(0, prfHg)
            .lineTo(0, 0)
            .height(lgt)
            .appearance(app);

    var strObject = cowner
            .createChild(strName)
            .parametric(dirName + strName + '.ptx', structure)
            .rotate(90, 90, 0)
            .translate(-lgt / 2.0, -0.5 - width / 2.0, height - tappDst - tappTh - prfHg);

    var tapp = box(width, tappTh, lgt).appearance(appTapp);
    strObject
            .createChild(tappetoName)
            .parametric(dirName + tappetoName + '.ptx', tapp)
            .rotate(0, 0, 0)
            .translate(width / 2.0, prfHg + tappDst + tappTh / 2.0, 0);

    if (cylAPresent) {
        var cylA = cylinder(cylRadius + tappDst + tappTh, width);
        cylA.appearance(appTapp);
        var cylAObject = cowner
                .createChild(cylAName)
                .parametric(dirName + cylAName + '.ptx', cylA)
                .rotate(90, 0, 0)
                .translate(-lgt / 2.0, width / 2.0, height - tappDst - tappTh - prfHg / 2.0);
    }

    if (cylBPresent) {
        var cylB = cylinder(cylRadius + tappDst + tappTh, width);
        cylB.appearance(appTapp);

        var cylBObject = cowner
                .createChild(cylBName)
                .parametric(dirName + cylBName + '.ptx', cylB)
                .rotate(90, 0, 0)
                .translate(+lgt / 2.0, width / 2.0, height - tappDst - tappTh - prfHg / 2.0);
    }
}


function lwLeg(cowner, count, x, y, cname) {

    var legName = cname + '_leg' + count;

    var legBox = box(legWidth, legWidth, height - tappDst - tappTh - prfHg).appearance(app);
    var legObject = cowner
            .createChild(legName)
            .parametric(dirName + legName + '.ptx', legBox)
            .translate(x, y, 0);

}

function crossbar(cowner, count, x, cname) {
    var crossName = cname + '_cross';
    var cBox = box(legWidth, width - legWidth, legWidth).appearance(app);
    var cObject = cowner
            .createChild(crossName)
            .parametric(dirName + crossName + '.ptx', cBox)
            .translate(x, 0, Math.max(height / 5, legConeHgt + legWidth));
}


