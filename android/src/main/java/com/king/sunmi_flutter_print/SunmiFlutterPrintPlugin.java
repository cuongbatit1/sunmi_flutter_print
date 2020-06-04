package com.king.sunmi_flutter_print;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.content.Context;
import android.os.RemoteException;

import com.sunmi.peripheral.printer.ExceptionConst;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.SunmiPrinterService;

/** SunmiFlutterPrintPlugin */
public class SunmiFlutterPrintPlugin implements MethodCallHandler {

  public static int NoSunmiPrinter = 0x00000000;
  public static int CheckSunmiPrinter = 0x00000001;
  public static int FoundSunmiPrinter = 0x00000002;
  public static int LostSunmiPrinter = 0x00000003;

  /**
   *  sunmiPrinter means checking the printer connection status
   */
  public int sunmiPrinter = CheckSunmiPrinter;
  /**
   *  SunmiPrinterService for API
   */
  private SunmiPrinterService sunmiPrinterService;


  private final MethodChannel channel;
  private Activity activity;
  private static final String TAG = "SUNMI_FLUTTER_PRINT";

  private Context context;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "sunmi_flutter_print");
    channel.setMethodCallHandler(new SunmiFlutterPrintPlugin(registrar.activity(),channel,registrar.context()));
    
  }

  /**
   *
   * @param activity
   * @param channel
   * @param context
   */
  private SunmiFlutterPrintPlugin(Activity activity, MethodChannel channel, Context context){
    this.activity = activity;
    this.channel = channel;
    this.channel.setMethodCallHandler(this);
    this.context = context; //not used when using onCreate
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) { result.success("Android " + android.os.Build.VERSION.RELEASE); }

    // INFORMATION
    else if(call.method.equals("getInfo")){
      int code = call.argument("code");
      result.success(this.getInfo(code));
    }

    // PRINTER INITIAL AND BINDING UNBINDING
    else if(call.method.equals("initPrinter")){ this.initPrinter(); }
    else if(call.method.equals("bindPrinter")){ this.bindingService(); }
    else if(call.method.equals("unbindPrinter")){ this.unBindService(); }
    else if(call.method.equals("selfCheckingPrinter")){ this.selfChecking(); }

    // STANDAR FUNCTION
    else if(call.method.equals("setAlignment")){
      int alignment = call.argument("alignment");
      this.setAlignment(alignment);
    }
    else if(call.method.equals("setFontName")){
      String fontName = call.argument("fontName");
      this.setFontName(fontName);
    }
    else if(call.method.equals("setFontSize")){
      int fontSize = call.argument("fontSize");
      this.setFontSize(fontSize);
    }
    else if(call.method.equals("setLineWrap")){
      int line = call.argument("line");
      this.setLineWrap(line);
    }
    else if(call.method.equals("printText")){
      String text = call.argument("text");
      this.printText(text);
    }
    else if(call.method.equals("printTextWithFont")){
      String text = call.argument("text");
      String fontName = call.argument("fontName");
      int fontSize = call.argument("fontSize");
      this.printTextWithFont(text,fontName,fontSize);
    }
    else if(call.method.equals("printOriginalText")){
      String text = call.argument("text");
      this.printOriText(text);
    }
    else if(call.method.equals("printColumnText")){
      String[] text = call.argument("text");
      int[] width = call.argument("width");
      int[] align = call.argument("align");
      this.printColumnText(text,width,align);
    }
    else if(call.method.equals("printQRCode")){
      String text = call.argument("text");
      int moduleSize = call.argument("moduleSize");
      int errorLevel = call.argument("errorLevel");
      this.printQRCode(text, moduleSize, errorLevel);
    }
    else if(call.method.equals("printBarcode")){
      String text = call.argument("text");
      int symbology = call.argument("symbology");
      int height = call.argument("height");
      int width = call.argument("width");
      int position = call.argument("textPosition");
      this.printBarcode(text,symbology,height,width,position);
    }
    else if(call.method.equals("printBitmap")){
      byte[] bytes = call.argument("bitmap");
      Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
      this.printBitmap(bitmap);
    }
    else if(call.method.equals("nextLine")){
        int line = call.argument("line");
        try {
            byte[] bytes = BytesUtil.nextLine(line);
            this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("initBlackBox")){
        int w = call.argument("width");
        int h = call.argument("height");
        try {
            byte[] bytes = BytesUtil.initBlackBlock(w,h);
          this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("initGrayBox")){
        int w = call.argument("width");
        int h = call.argument("height");
        try {
            byte[] bytes = BytesUtil.initGrayBlock(w,h);
          this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("initTable")){
        int w = call.argument("width");
        int h = call.argument("height");
        try {
            byte[] bytes = BytesUtil.initTable(w,h);
            this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("underline1Dot")){
        try {
            byte[] bytes = BytesUtil.underlineWithOneDotWidthOn();
            this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("underline2Dot")){
        try {
            byte[] bytes = BytesUtil.underlineWithTwoDotWidthOn();
            this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("underlineOff")){
        try {
            byte[] bytes = BytesUtil.underlineOff();
            this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("boldOn")){
        try {
            byte[] bytes = BytesUtil.boldOn();
            this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("boldOff")){
        try {
            byte[] bytes = BytesUtil.boldOff();
            this.sendRAWData(bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    else if(call.method.equals("autoOutPaper")){
      this.feedPaper();
    }

    // NOT FOUND
    else { result.notImplemented(); }
  }

  private InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
    @Override
    protected void onConnected(SunmiPrinterService service) {
      sunmiPrinterService = service;
      checkSunmiPrinterService(service);
    }

    @Override
    protected void onDisconnected() {
      sunmiPrinterService = null;
      sunmiPrinter = LostSunmiPrinter;
    }
  };

  /**
   * Check the printer connection,
   * like some devices do not have a printer but need to be connected to the cash drawer through a print service
   */
  private void checkSunmiPrinterService(SunmiPrinterService service){
    boolean ret = false;
    try {
      ret = InnerPrinterManager.getInstance().hasPrinter(service);
    } catch (InnerPrinterException e) {
      e.printStackTrace();
    }
    sunmiPrinter = ret?FoundSunmiPrinter:NoSunmiPrinter;
  }

  /**
   *  Some conditions can cause interface calls to fail
   *  For example: the version is too low、device does not support
   *  You can see {@link ExceptionConst}
   *  So you have to handle these exceptions
   */
  private void handleRemoteException(RemoteException e){
    //TODO process when get one exception
  }

  /**
   * ================================== BINDING/UNBIND APPLICATION =================================
   */
  public void bindingService(){
    try {
      boolean ret =  InnerPrinterManager.getInstance().bindService(context,
              innerPrinterCallback);
      if(!ret){
        sunmiPrinter = NoSunmiPrinter;
      }
    } catch (InnerPrinterException e) {
      e.printStackTrace();
    }
  }
  public void unBindService(){
    try {
      if(sunmiPrinterService != null){
        InnerPrinterManager.getInstance().unBindService(context, innerPrinterCallback);
        sunmiPrinterService = null;
        sunmiPrinter = LostSunmiPrinter;
      }
    } catch (InnerPrinterException e) {
      e.printStackTrace();
    }
  }

  /**
   *  Initialize the printer
   *  All style settings will be restored to default
   */
  public void initPrinter(){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.printerInit(null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   */
  public void selfChecking(){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.printerSelfChecking(null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param code 1,2,3,4
   * @return String
   */
  public String getInfo(int code){
    String output = null;
    if(code == 1){
      output = getPrinterSerialNo();
    } else if(code == 2){
      output = getDeviceModel();
    } else if(code == 3){
      output = getPrinterVersion();
    } else if(code == 4){
      output = getPrinterPaper();
    } else {
      output = "No information found.";
    }
    return output;
  }

  /**
   * Get printer serial number
   */
  public String getPrinterSerialNo(){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return "";
    }
    try {
      return sunmiPrinterService.getPrinterSerialNo();
    } catch (RemoteException e) {
      handleRemoteException(e);
      return "";
    }
  }

  /**
   * Get device model
   */
  public String getDeviceModel(){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return "";
    }
    try {
      return sunmiPrinterService.getPrinterModal();
    } catch (RemoteException e) {
      handleRemoteException(e);
      return "";
    }
  }

  /**
   * Get firmware version
   */
  public String getPrinterVersion(){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return "";
    }
    try {
      return sunmiPrinterService.getPrinterVersion();
    } catch (RemoteException e) {
      handleRemoteException(e);
      return "";
    }
  }

  /**
   * Get paper specifications
   */
  public String getPrinterPaper(){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return "";
    }
    try {
      return sunmiPrinterService.getPrinterPaper() == 1?"58mm":"80mm";
    } catch (RemoteException e) {
      handleRemoteException(e);
      return "";
    }
  }

  /**
   *
   * @param bytes
   */
  public void sendRAWData(final byte[] bytes){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.sendRAWData(bytes, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param alignment
   */
  public void setAlignment(final int alignment){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.setAlignment(alignment, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param fontName
   */
  public void setFontName(final String fontName){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.setFontName(fontName, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param fontSize
   */
  public void setFontSize(final float fontSize){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.setFontSize(fontSize, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param line
   */
  public void setLineWrap(final int line){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.lineWrap(line, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param text
   */
  public void printText(final String text){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.printText(text, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param text
   */
  public void printOriText(final String text){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.printOriginalText(text, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param text
   * @param fontName
   * @param fontSize
   */
  public void printTextWithFont(final String text, final String fontName, final float fontSize){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.printTextWithFont(text,fontName,fontSize, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   * Unsupported Arabic
   * @param textArray
   * @param widthArray
   * @param alignArray
   */
  public void printColumnText(final String[] textArray, final int[] widthArray, final int[] alignArray){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.printColumnsText(textArray,widthArray,alignArray, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

  /**
   *
   * @param bitmap
   */
  public void printBitmap(final Bitmap bitmap){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }
    try {
      sunmiPrinterService.printBitmap(bitmap, null);
      sunmiPrinterService.lineWrap(3, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

//  /**
//   *
//   * @param data
//   * @param size
//   */
//  public void printQRCodeWithZxing(final String data, final int size){
//    ThreadPoolManager.getInstance().executeTask(new Runnable() {
//      @Override
//      public void run() {
//        try {
//          byte[] bytes = BytesUtil.getZXingQRCode(data,size);
//          woyouService.sendRAWData(bytes,callback);
//        } catch (RemoteException e){
//          e.printStackTrace();
//        }
//      }
//    });
//  }

  /**
   *
   * @param data
   * @param moduleSize
   * @param errorLevel
   * Data →qr code content.
   * Modulesize →qr code block size, unit: dot, values 4 to 16.
   * Errorlevel qr code error correction level (0-3) :
   * 0→ error correction level L (7%)
   * 1→error correction level M (15%)
   * 2→ error correction level Q (25%)
   * 3→error correction level H (30%)
   */
  public void printQRCode(final String data, final int moduleSize, final int errorLevel){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }

    try {
      sunmiPrinterService.printQRCode(data, moduleSize, errorLevel, null);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * @param data
   * @param symbology 0 -> 8
   * @param height 1 - 255, default 162
   * @param width 2 - 6, default 2
   * @param textpos 0 - 3 : 0 →no print text, 1→ above the barcode, 2→ below the barcode, 3→ both
   *                Data →one-dimensional code content.
   * 上海商米科技有限公司打印机开发者文档
   * - 13 -
   * Symbology→ barcode type (0-8) :
   * 0 → UPC-A
   * 1 → UPC-E
   * 2 → JAN13(EAN13)
   * 3 → JAN8(EAN8)
   * 4 → CODE39
   * 5 → ITF
   * 6 → CODABAR
   * 7 → CODE93
   * 8 → CODE128
   * Height →bar code height, value 1-255, default: 162.
   * Width →bar code width, value 2-6, default: 2.
   * TextPosition →text Position (0-3) :
   * 0 →no print text
   * 1→ above the barcode
   * 2→ below the barcode
   * 3→ both
   */
  public void printBarcode(final String data, final int symbology, final int height, final int width, final int textpos){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }

    try {
      sunmiPrinterService.printBarCode(data, symbology, height, width, textpos, null);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  public void feedPaper(){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }

    try {
      sunmiPrinterService.autoOutPaper(null);
    } catch (RemoteException e) {
      print3Line();
    }
  }

  /**
   *  paper feed three lines
   *  Not disabled when line spacing is set to 0
   */
  public void print3Line(){
    if(sunmiPrinterService == null){
      //TODO Service disconnection processing
      return;
    }

    try {
      sunmiPrinterService.lineWrap(3, null);
    } catch (RemoteException e) {
      handleRemoteException(e);
    }
  }

}
