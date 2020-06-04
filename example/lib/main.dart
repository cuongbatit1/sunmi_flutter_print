import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:sunmi_flutter_print/sunmi_flutter_print.dart';

import 'package:sunmi_flutter_print/models.dart';


void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    initBindPrinter();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await SunmiFlutterPrint.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  initBindPrinter() async {
    await SunmiFlutterPrint.bindPrinter();
  }

  _printQR() {
    SunmiFlutterPrint.setAlignment(align : TEXTALIGN.CENTER);
    SunmiFlutterPrint.printQRCode(text: "AAAAAAAAA", moduleSize: 7);
    SunmiFlutterPrint.autoOutPaper();

    
  }

  _printBarCode() {

    SunmiFlutterPrint.printBarcode("201705070507", 120 , 2, textPosition: TEXTPOS.ABOVE_BARCODE, symbology: SYMBOLOGY.CODE_128);
    SunmiFlutterPrint.autoOutPaper();
  }

  @override
  void dispose() {
    SunmiFlutterPrint.unbindPrinter();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[Center(
            child: Text('Running on: $_platformVersion\n'),
          ),
            Container(
              width: 100,
              height: 50,
              color: Colors.red,
              child: Material(
                  color: Colors.transparent,
                  child: InkWell(
                    onTap: () {
                      _printBarCode();
                    },
                    child: Text("Print bar code"),
                  )),
            ),
            Container(
              width: 100,
              height: 50,
              color: Colors.green,
              child: Material(
                  color: Colors.transparent,
                  child: InkWell(
                    onTap: () {
                      _printQR();
                    },
                    child: Text("Print QR code"),
                  )),
            )],
        )


      ),
    );
  }
}
