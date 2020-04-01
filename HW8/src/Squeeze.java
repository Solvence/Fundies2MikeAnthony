import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;


class SeamInfo {
  
  Pixel thisPixel;
  double totalWeight;
  SeamInfo cameFrom;
  
  
}

class Picture extends World {
  APixel topLeft;
  
//  Picture(String imgFileName) {
//    FromFileImage img = new FromFileImage("balloons.jpg");
//    topLeft = new SentinelPixel();
//    APixel prevRowPixel = topLeft;
//    for (int row = 0; row < 343; row += 1) {
//      APixel newPrevRowPixel = new SentinelPixel();
//      newPrevRowPixel.up = prevRowPixel;
//      newPrevRowPixel.down = prevRowPixel.down;
//      newPrevRowPixel.up.down = newPrevRowPixel;
//      newPrevRowPixel.down.up = newPrevRowPixel;
//      prevRowPixel = newPrevRowPixel;
//      APixel prevPixel = prevRowPixel;
//      for (int col = 0; col < 800; col += 1) {
//        Pixel thisPixel = new Pixel(prevPixel.up.right, new SentinelPixel(), prevPixel, 
//            prevPixel.right, img.getColorAt(row, col));
//        if (row == 0) {
//          SentinelPixel newSent = new SentinelPixel();
//          newSent.left = prevPixel.up;
//          newSent.left = prevPixel.up.right;
//          prevPixel.up.right = prevPixel.up;
//          newSent.right.left = newSent;
//          thisPixel.up = newSent;
//          
//          thisPixel.down = thisPixel.up;
//        } else {
//          thisPixel.up = prevPixel.up.right;
//          thisPixel.down = thisPixel.up.down;
//        }
//        thisPixel.up.down = thisPixel;
//        thisPixel.down.up = thisPixel;
//        thisPixel.left.right = thisPixel;
//        thisPixel.right.left = thisPixel;
//        prevPixel = thisPixel;
//      }
//    }
//  }
  
  Picture(String imgFileName) {
    
    ArrayList<SentinelPixel> rows = new ArrayList<SentinelPixel>();
    ArrayList<SentinelPixel> cols = new ArrayList<SentinelPixel>();
    
    for (int i = 0; i < 344; i += 1) {
      rows.add(new SentinelPixel());
    }
    
    for (int i = 0; i < 800; i += 1) {
      
      cols.add(new SentinelPixel());
      
    }
    
    for (int i = 0; i < 344; i++) {
      
      for (int j = 0; i < 800; i++) {
        
        SentinelPixel rowSentinel = rows.get(i);
        SentinelPixel colSentinel = cols.get(j);
        
        rowSentinel.addAtHead(Color.red);
        colSentinel.addAtHead(Color.red);
        
        
      }
      
    }
    
    FromFileImage img = new FromFileImage("balloons.jpg");
    
    
  }
  
  public WorldScene makeScene() {
    WorldImage img = new FromFileImage("balloons.jpg");
    
    WorldScene ws = new WorldScene(800, 344);
    ws.placeImageXY(img, 400, 172);
    
    return ws;
  }
}

abstract class APixel {
  APixel up;
  APixel down;
  APixel left;
  APixel right;
  Color color;
  
  APixel(APixel up, APixel down, APixel left, APixel right, Color color) {
    this.up = up;
    this.down = down;
    this.left = left;
    this.right = right;
    this.color = color;
  }
  
  APixel() {
    this.up = null;
    this.down = null;
    this.left = null;
    this.right = null;
    this.color = Color.BLACK;
  }
  
  double calculateBrightness() {
    return ((double) (color.getRed() + color.getGreen() + color.getBlue())) / 3 / 255.0;
  }
  
  void setUp(APixel newUp) {
    this.up = newUp;
  }
  
  void setDown(APixel newDown) {
    this.down = newDown;
  }
  
  void setLeft(APixel newLeft) {
    this.left = newLeft;
  }
  
  void setRight(APixel newRight) {
    this.right = newRight;
  }
}

class SentinelPixel extends APixel {
  SentinelPixel(Pixel up, Pixel down, Pixel left, Pixel right) {
    super(up, down, left, right, Color.BLACK);
  }
  
  SentinelPixel() {
    super();
    this.up = this;
    this.down = this;
    this.left = this;
    this.right = this;
    
    this.color = Color.BLACK;
  }
  
  void addAtHead(Color color) {
    new Pixel(this.up, this.down, this.left, this.right, color);
  }
}

class Pixel extends APixel {
  
//  Pixel(APixel up, APixel down, APixel left, APixel right, Color color) {
//    super(up, down, left, right, color);
//  }
  
  Pixel(APixel up, APixel down, APixel left, APixel right, Color color) {
    super(up, down, left, right, color);
    
    this.up.setDown(this);
    this.down.setUp(this);
    this.left.setRight(this);
    this.right.setLeft(this);
  }
  
  
  
  double calculateHorizEnergy() {
    
    return (this.up.left.calculateBrightness() +
        2 * this.left.calculateBrightness() +
        this.down.left.calculateBrightness()) - (
        this.up.right.calculateBrightness() +
        2 * this.right.calculateBrightness() +
        this.down.right.calculateBrightness());
        
        
  }
  
  double calculateVertEnergy() {
    
    return (this.up.left.calculateBrightness() +
        2 * this.up.calculateBrightness() +
        this.up.right.calculateBrightness()) - (
        this.down.left.calculateBrightness() +
        2 * this.down.calculateBrightness() +
        this.down.right.calculateBrightness());
        
  }
  
  double calculateEnergy() {
    return Math.sqrt(Math.pow(this.calculateHorizEnergy(), 2) +
        Math.pow(this.calculateVertEnergy(), 2));
  }
  
  
}


class ExamplesSqueeze {
  
  // TODO test field of field on pixel neighbors
  
  void testWorld (Tester t) {
    
    Picture p = new Picture("balloons.jpg");
    p.bigBang(800, 344);
        
    
  }
  
}