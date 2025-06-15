function customize() {
    var dst = "generated/" + instance.uuid + "/signalsmap.xml";
    copy("signalsmap.xml",  dst);		
    parameter('map', dst);	
    return true;
}
