import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

// Represents a Seam Info. A Seam info describes a the information regarding a continuous path
// between edges of a Picture
class SeamInfo {
  APixel thisPixel;
  double totalWeight;
  SeamInfo cameFrom;

  // the constructor
  SeamInfo(APixel thisPixel, double totalWeight, SeamInfo cameFrom) {
    this.thisPixel = thisPixel;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;
  }

  // constructor that only takes in two fields, used for the first row of pixels,
  // when the seam has nothing it came from
  SeamInfo(APixel thisPixel, double totalWeight) {
    this.thisPixel = thisPixel;
    this.totalWeight = totalWeight;
    this.cameFrom = null;
  }
}

// Represents the world state of a Picture being compressed
class Picture extends World {
  APixel topLeft;  // the top left SentinelPixel of this picture. It is the actual top left pixel's
  // top left
  int width;
  int height;
  SeamInfo seamToRemove; // the seam to remove in the current tick. If there is no seam to remove
  // currently, is either null or has a null cameFrom

  // Constructs a Picture and Transforms it into a 2D pixel deque that can be used for seam removal.
  Picture(String imgFileName) {
    FromFileImage img = new FromFileImage(imgFileName);
    this.width = (int) img.getWidth();
    this.height = (int) img.getHeight();
    topLeft = new SentinelPixel();
    APixel prevRowPixel = topLeft;
    for (int row = 0; row < img.getHeight(); row += 1) {
      APixel newPrevRowPixel = new SentinelPixel();
      newPrevRowPixel.up = prevRowPixel;
      newPrevRowPixel.down = prevRowPixel.down;
      newPrevRowPixel.up.down = newPrevRowPixel;
      newPrevRowPixel.down.up = newPrevRowPixel;
      prevRowPixel = newPrevRowPixel;
      APixel prevPixel = prevRowPixel;
      for (int col = 0; col < img.getWidth(); col += 1) {
        Pixel thisPixel = new Pixel(prevPixel.up.right, new SentinelPixel(), prevPixel,
            prevPixel.right, img.getColorAt(col, row));
        if (row == 0) {
          SentinelPixel newSent = new SentinelPixel();
          newSent.left = prevPixel.up;
          newSent.right = prevPixel.up.right;
          prevPixel.up.right = newSent;
          newSent.right.left = newSent;
          thisPixel.up = newSent;
          thisPixel.down = newSent;
        }
        else {
          thisPixel.up = prevPixel.up.right;
          thisPixel.down = thisPixel.up.down;
        }
        thisPixel.up.down = thisPixel;
        thisPixel.down.up = thisPixel;
        thisPixel.left.right = thisPixel;
        thisPixel.right.left = thisPixel;
        prevPixel = thisPixel;
      }
    }
  }

  // renders this Picture as an image
  public WorldScene makeScene() {

    ComputedPixelImage cpi = new ComputedPixelImage(this.width, this.height);
    APixel nextRowPixel = topLeft;
    for (int row = 0; row < this.height; row += 1) {
      nextRowPixel = nextRowPixel.down;
      APixel nextPixel = nextRowPixel.right;
      for (int col = 0; col < this.width; col += 1) {
        cpi.setPixel(col, row, nextPixel.color);
        nextPixel = nextPixel.right;
      }
    }

    WorldScene ws = new WorldScene(this.width, this.height);

    ws.placeImageXY(cpi, this.width / 2, this.height / 2);

    return ws;
  }

  // Method that gets run every tick. Either removes a seam or identifies and highlights a seam 
  // to be removed the next time onTick is called. 
  // EFFECT: if the seam is being removed on this tick, modifies the pixels in this picture to
  // exclude the pixels being removed, and modifies the seamToRemove seam field to have a null
  // cameFrom field (to let the next tick know that the seam has already been removed)
  // if the seam is being highlighted on this tick, modifies the pixels in the seam to have a red
  // color, and modifies the seamToRemove field to contain the seam to be removed in the next tick
  public void onTick() {

    if (this.width <= 1) {
      this.endOfWorld("");
    }

    if (this.seamToRemove != null && this.seamToRemove.cameFrom != null) {
      this.removeSeam();
    } else {
      ArrayList<ArrayList<SeamInfo>> seams = new ArrayList<ArrayList<SeamInfo>>();
      this.updateSeams(seams);
      
      // finds the SeamInfo in the bottom row with the least total weight
      SeamInfo seamToHighlight = seams.get(height - 1).get(0);
      for (int col = 1; col < this.width; col += 1) {
        if (seams.get(height - 1).get(col).totalWeight < seamToHighlight.totalWeight) {
          seamToHighlight = seams.get(height - 1).get(col);
        }
      }
      
      // updates the seamToRemove field so that, in the next tick, this seam will be removed from
      // this image
      this.seamToRemove = seamToHighlight;

      // highlights the seam in red
      while (seamToHighlight.cameFrom != null) {
        seamToHighlight.thisPixel.highlight();
        seamToHighlight = seamToHighlight.cameFrom;
      }
    }
  }
  
  // removes the seam labeled by the seamToRemove field from this Picture
  // EFFECT: alters seamToRemove to have a null cameFrom, adjusts the pixels representing this
  // Picture to account for the removed seam
  void removeSeam() {
    APixel currentPixel = this.seamToRemove.thisPixel;
    currentPixel.down.remove();
    while (this.seamToRemove.cameFrom != null) {
      currentPixel = this.seamToRemove.thisPixel;
      currentPixel.remove();
      this.seamToRemove = this.seamToRemove.cameFrom;
    }
    currentPixel.remove();
    currentPixel.up.remove();
    width -= 1;
  }
  
  // fills the given 2D SeamInfo array with the proper SeamInfos from this Picture
  // EFFECT: modifies the given SeamInfo array to contain the corresponding SeamInfo for every pixel
  // in this Picture
  void updateSeams(ArrayList<ArrayList<SeamInfo>> seams) {
    APixel nextRowPixel = topLeft;
    for (int row = 0; row < this.height; row += 1) {
      nextRowPixel = nextRowPixel.down;
      APixel nextPixel = nextRowPixel.right;
      seams.add(new ArrayList<SeamInfo>());
      for (int col = 0; col < this.width; col += 1) {
        if (row == 0) {
          seams.get(row).add(new SeamInfo(nextPixel, nextPixel.calculateEnergy()));
        }
        else {
          SeamInfo cameFrom = seams.get(row - 1).get(col);
          if (col != 0 && seams.get(row - 1).get(col - 1).totalWeight < cameFrom.totalWeight) {
            cameFrom = seams.get(row - 1).get(col - 1);
          }
          if (col != width - 1
              && seams.get(row - 1).get(col + 1).totalWeight < cameFrom.totalWeight) {
            cameFrom = seams.get(row - 1).get(col + 1);
          }

          seams.get(row).add(new SeamInfo(nextPixel, nextPixel.calculateEnergy(), cameFrom));
        }
        nextPixel = nextPixel.right;
      }
    }
  }
}

// Represents a pixel
abstract class APixel {
  APixel up;
  APixel down;
  APixel left;
  APixel right;
  Color color;

  // Initializes a pixel and places it in the grid of pixels surrounded by these pixels 
  APixel(APixel up, APixel down, APixel left, APixel right, Color color) {
    this.up = up;
    this.down = down;
    this.left = left;
    this.right = right;
    this.color = color;
  }

  // Initializes a stand alone pixel. 
  APixel() {
    this.up = null;
    this.down = null;
    this.left = null;
    this.right = null;
    this.color = Color.BLACK;
  }

  // Calculates the brightness of a pixel
  double calculateBrightness() {
    return ((double) (color.getRed() + color.getGreen() + color.getBlue())) / 3 / 255.0;
  }

  // Calculates the horizontal energy of a pixel from its surroundings
  double calculateHorizEnergy() {
    return (this.up.left.calculateBrightness() + 2 * this.left.calculateBrightness()
        + this.down.left.calculateBrightness())
        - (this.up.right.calculateBrightness() + 2 * this.right.calculateBrightness()
            + this.down.right.calculateBrightness());
  }

  // Calculates the vertical energy of a pixel from its surroundings
  double calculateVertEnergy() {
    return (this.up.left.calculateBrightness() + 2 * this.up.calculateBrightness()
        + this.up.right.calculateBrightness())
        - (this.down.left.calculateBrightness() + 2 * this.down.calculateBrightness()
            + this.down.right.calculateBrightness());
  }

  // Calculates the overall energy of a pixel from its surroundings
  double calculateEnergy() {
    return Math
        .sqrt(Math.pow(this.calculateHorizEnergy(), 2) + Math.pow(this.calculateVertEnergy(), 2));
  }

  // Removes this pixel from the grid of pixels in this picture
  void remove() {
    this.up.down = this.down;
    this.down.up = this.up;
    this.left.right = this.right;
    this.right.left = this.left;
  }

  // Highlights this pixel 
  void highlight() {
    this.color = Color.RED;
  }
}

// Guards the list of pixels while acting as a black pixel, useful as an Edge in 2D Pixel Deque
class SentinelPixel extends APixel {
  
  // Initializes a sentinel pixel and places it into the grid of pixels 
  SentinelPixel(Pixel up, Pixel down, Pixel left, Pixel right) {
    super(up, down, left, right, Color.BLACK);
  }

  // Initializes a standalone sentinel, acting as an empty Deque. 
  SentinelPixel() {
    super();
    this.up = this;
    this.down = this;
    this.left = this;
    this.right = this;

    this.color = Color.BLACK;
  }
}

// Represents a Pixel in a Grid of Pixels 
class Pixel extends APixel {

  // Creates a Pixel and places it in a grid of pixels surrounded by these pixels. 
  Pixel(APixel up, APixel down, APixel left, APixel right, Color color) {
    super(up, down, left, right, color);
  }
  
  // Creates a Pixel and places it in a grid of pixels surrounded by these pixels. 
  Pixel() {
    super(null, null, null, null, Color.BLACK);
  }

}

// Helper class for creating grids of pixels without making a Picture
// used for tests
class PixelGrid extends World {
  // Renders a grid of pixels, used for testing
  public WorldScene makeScene() {
    ComputedPixelImage cpi = new ComputedPixelImage(400, 400);
    
    cpi.setPixels(0, 0, 400, 400, Color.red);
    
    cpi.saveImage("redBoard.jpg");
    
    WorldScene ws = new WorldScene(400, 400);

    ws.placeImageXY(cpi, 400 / 2, 400 / 2);
    
    return ws;
  }
}

// Examples class for testing
class ExamplesSqueeze {

  // TODO test field of field on pixel neighbors
  Picture p;
  Picture p3;
  APixel surroundedPixel;

  // Initializes conditions for testing
  void initTestConditions() {
    p = new Picture("balloons.jpg");
    p3 = new Picture("3by3img.jpg");
  }
  // initializes a pixel grid
  void initPixelGrid() {
    
    Picture p2 = new Picture("balloons.jpg");
    
    surroundedPixel = p2.topLeft.down.right.down.right;
    surroundedPixel.up.right.color = new Color(5, 5, 5);
    surroundedPixel.up.left.color = new Color(10, 10, 10);
    surroundedPixel.up.color = new Color(15, 15, 15);
    surroundedPixel.color = new Color(20, 20, 20);
    surroundedPixel.right.color = new Color(30, 30, 30);
    surroundedPixel.left.color =  new Color(40, 40, 40);
    surroundedPixel.down.color = new Color(50, 50, 50);
    surroundedPixel.down.right.color = new Color(60, 60, 60);
    surroundedPixel.down.left.color = new Color(100, 50, 0);
  }
  

  // Runs the Seam Removal Program
  void testWorld(Tester t) {
    initTestConditions();
    p.bigBang(800, 343, 1.0 / 28);
  }

  /*
   * Testing WishList
   * 
   * calculateBrightness - Brightness is correctly calculated
   * 
   * highlight - Highlighted pixels have their colors properly changed to red
   * Highlighted pixels do not effect the energy computation of the rest of the
   * picture. (Can be demonstrated by running with different highlight colors and
   * checking to see if resulting image at different iteration counts is constant
   * ) Sentinel Pixels should not be highlighted as they should be black for
   * calculations
   * 
   * calculateEnergy - Horizontal Energy is correctly computed Vertical Energy is
   * correctly computed Total energy is correctly computed from Horizontal and
   * vertical energy Energy works with both SentinelPixels and Normal Pixels
   * (Sentinel Pixels treated as black)
   * 
   * Remove - Remove correctly modifies the references of the surrounding pixels
   * Removing a sentinel doesn't do anything When all pixels are removed the
   * sentinel refers to itself in all four directions ???
   * 
   * On Tick -
   * 
   * The world ends when width is less than or equal to 1 The world's width
   * decreases every other tick
   */

  // Check the On Tick Functionality 
  void testOnTick(Tester t) {

    this.initTestConditions();
    this.initPixelGrid();

    int initWidth = p.width;
    p.onTick();
    int updatedWidth = p.width;
    // Width is not updated on the first tick
    t.checkExpect(initWidth, updatedWidth);

    // Width is updated on second tick
    this.initTestConditions();
    p.onTick();
    p.onTick();
    updatedWidth = p.width;
    t.checkExpect(initWidth - 1, updatedWidth);
  }
  
  // tests the updateSeams method in the Picture class
  void testUpdateSeams(Tester t) {
    this.initTestConditions();
    ArrayList<ArrayList<SeamInfo>> seamTestArr = new ArrayList<ArrayList<SeamInfo>>();
    
    // tests that the seam array has nothing in it initially
    t.checkExpect(seamTestArr.size(), 0);
    
    p3.updateSeams(seamTestArr);
    
    // tests the expected changes within the seam array
    t.checkInexact(seamTestArr.get(0).get(0), new SeamInfo(p3.topLeft.right.down, 0.438444, null), 
        .001);
    t.checkInexact(seamTestArr.get(0).get(1), 
        new SeamInfo(p3.topLeft.right.down.right, 0.438444, null), 
        .001);
    t.checkInexact(seamTestArr.get(0).get(2), 
        new SeamInfo(p3.topLeft.right.down.right.right, 0.36996004, null), 
        .001);
    t.checkInexact(seamTestArr.get(1).get(0), 
        new SeamInfo(p3.topLeft.right.down.down, 0.6106828040, 
            new SeamInfo(p3.topLeft.right.down, 0.438444, null)), 
        .001);
    t.checkInexact(seamTestArr.get(1).get(1), 
        new SeamInfo(p3.topLeft.right.down.down.right, 0.649727118658, 
            new SeamInfo(p3.topLeft.right.down.right.right, 0.36996004, null)), 
        .001);
  }
  
  // test the removeSeam method in the Picture class
  void testRemoveSeam(Tester t) {
    
  }

  // Check the pixel removal functionality
  void testRemove(Tester t) {
    APixel sp = new SentinelPixel();
    APixel sp2 = new SentinelPixel();
    sp.remove();
    t.checkExpect(sp, sp2);
//    
//    APixel pixel = new Pixel(sp, sp, sp, sp, Color.DARK_GRAY);
//    pixel.remove();
//    t.checkExpect(pixel, sp);

  }

  // Test Brightness calculations
  void testCalculateBrightness(Tester t) {
    APixel sp = new SentinelPixel();
    t.checkInexact(sp.calculateBrightness(), 0.0, 0.001);

    APixel pixel = new Pixel(sp, sp, sp, sp, Color.BLUE);
    t.checkInexact(pixel.calculateBrightness(), 1.0 / 3, 0.001);

    pixel = new Pixel(sp, sp, sp, sp, Color.white);
    t.checkInexact(pixel.calculateBrightness(), 1.0, 0.001);

    pixel = new Pixel(sp, sp, sp, sp, new Color(85, 170, 255));
    t.checkInexact(pixel.calculateBrightness(), 2.0 / 3, 0.001);

  }
  
  
  

  
  // test the energy calculation for a pixel's surroundings. 
  // this includes tests for all three methods calculateHorizEnergy, calculateVertEnergy, 
  // and calculateEnergy
  void testCalculateEnergy(Tester t) {
    APixel sp = new SentinelPixel();
    APixel pixel = new Pixel(sp, sp, sp, sp, Color.BLUE);
    // A Pixel Surrounded By Sentinels should have 0 Horizontal, Vertical, Or Overall Energy
    t.checkInexact(pixel.calculateHorizEnergy(), 0.0, 0.001);
    t.checkInexact(pixel.calculateVertEnergy(), 0.0, 0.001);
    t.checkInexact(pixel.calculateEnergy(), 0.0, 0.001);
    
    initPixelGrid();
    
    t.checkInexact(surroundedPixel.calculateHorizEnergy(), 0.0588, 0.01);
    t.checkInexact(surroundedPixel.calculateVertEnergy(), -0.647, 0.01);
    t.checkInexact(surroundedPixel.calculateEnergy(), Math.sqrt(Math.pow(0.0588, 2) 
        + Math.pow(-0.647, 2)), 0.01);
    
    System.out.println("\n");
    System.out.println(surroundedPixel.color);
    System.out.println(surroundedPixel.right.color);
    System.out.println(surroundedPixel.left.color);
  }
}