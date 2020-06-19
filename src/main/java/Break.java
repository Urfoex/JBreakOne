// import java.nio.ByteBuffer;
// import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
// import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
// import javafx.scene.control.Label;
import javafx.scene.image.PixelFormat;
// import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class Break extends Application {
    final Point2D screen = new Point2D(640, 480);
    private GraphicsContext context = null;

    public class Player {
        public Point2D position = new Point2D(screen.getX()/2, screen.getY()-20);
        public double size = 30;
        public Point2D center() {
            return Break.center(position, new Point2D(size, size));
        }
        public void draw() {
            rect(position, new Point2D(size, 10), Color.FORESTGREEN);
        }

        public void setPosition(double x) {
            position = new Point2D(x-size/2, position.getY());
        }
    }

    public Point2D stoneSize = new Point2D(Math.floor(screen.getX()/22), 10);
    public interface HackStoneType{
        public enum StoneType {
            Level1,
            Level2,
            Level3,
            Level4,
            Level5,
            Level6,
            Level7,
            Level8,
        }
    }
    public class Stone implements HackStoneType {
        public Point2D position = new Point2D(0, 0);
        public StoneType type = StoneType.Level8;
        public int health = 99;

		public Stone(double x, double y, int type) {
            position = new Point2D(x*stoneSize.getX(), y*stoneSize.getY());
            this.type = StoneType.values()[type];
            health = type + 1;
		}
        public Point2D center() {
            return Break.center(position, stoneSize);
        }

		public void draw() {
            var color = Color.RED;
            switch(type){
                case Level1: color = Color.BLUEVIOLET; break;
                case Level2: color = Color.GREENYELLOW; break;
                case Level3: color = Color.PAPAYAWHIP; break;
                case Level4: color = Color.BURLYWOOD; break;
                case Level5: color = Color.AQUAMARINE; break;
                case Level6: color = Color.CHARTREUSE; break;
                case Level7: color = Color.DEEPPINK; break;
                case Level8: color = Color.FIREBRICK; break;
                default: color = Color.RED; break;
            };
            rect(position, stoneSize, color);
		}
    }

    static public Point2D center(Point2D position, Point2D size) {
        return position.add(size.multiply(0.5));
    }

    public class Ball {
        public double size = 10;
        public double initSpeed = 10;
        public Point2D velocity = new Point2D(0,0);
        public Point2D position = new Point2D(
            player.position.getX()+player.size/2-size/2,
            player.position.getY()-size);

        public Point2D center() {
            return Break.center(position, new Point2D(size, size));
        }

        public void draw() {
            if(position.getX() <= 0 || position.getX() >= screen.getX()
                    ||
                    position.getY() <= 0 || position.getY() >= screen.getY()
              ){
                System.out.println("CRAAAAAAAAAAAAAP!");
            }
            rect(position, new Point2D(size, size), Color.BLACK);
        }

        public void initVelocity() {
            var playerCenter = player.position.add(player.size/2, 0);
            velocity = center().subtract(player.center());
            velocity = velocity.normalize().multiply(initSpeed);
        }

		public void update() {
            // System.out.println(String.format("xy:%s  dxy:%s ", position, velocity));
            bounceX();
            bounceY();
            // System.out.println(String.format("xy:%s  dxy:%s ", position, velocity));
		}

        public Pair<Double, Double> bounced(double x, double dx, double minX, double maxX) {
            if(maxX - x <= dx){
                return Pair.of(maxX-(dx-(maxX-x)), -dx);
            }
            if(x-minX  <= -dx){
                // System.out.println(String.format("x:%s %s %s dx:%s %s", x, minX, x-minX, dx, minX+(-dx-(x-minX))));
                return Pair.of(minX+(-dx-(x-minX)), -dx);
            }
            return Pair.of(x+dx, dx);
        }

		public void bounceX() {
            var posVel = bounced(
                position.getX(), velocity.getX(),
                0+size/2, screen.getX()-size*1.5
            );
            position = new Point2D(posVel.getLeft(), position.getY());
            velocity = new Point2D(posVel.getRight(), velocity.getY());
		}

		public void bounceY() {
            var posVel = bounced(
                position.getY(), velocity.getY(),
                0+size/2, screen.getY()-size*1.5
            );
            position = new Point2D(position.getX(), posVel.getLeft());
            velocity = new Point2D(velocity.getX(), posVel.getRight());
		}

    }

    public Player player = new Player();
    public Ball ball = new Ball();
    public Stone[] stones = new Stone[0];


    public static void main(String[] args) {
        System.out.println("Starting Break...");
        launch();
        System.out.println("Break done.");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Break");
        var root = new Group();
        var canvas = new Canvas(640, 480);
        context = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        var scene = new Scene(root);
        primaryStage.setScene(scene);
        handleEvents(canvas);
        createLevel();
        startTimer();
        primaryStage.show();
    }

    public void createLevel() {
        stones = IntStream.range(1, (int)(screen.getY()/stoneSize.getY()-1-10))
        .mapToObj(iy ->
            IntStream.range(1, (int)(screen.getX()/stoneSize.getX()-1))
            .mapToObj(ix -> new int[]{ix, iy})
            .filter(o -> Math.random() > 0.5)
            .toArray()
            // .toArray(Integer[][]::new)
        )
        .flatMap(e -> Arrays.stream(e))
        .map(e -> (int[])e)
        .map(e -> new Stone(e[0], e[1], (int)Math.floor(Math.random()*Stone.StoneType.values().length)))
        .toArray(Stone[]::new);
    }

    public void handleEvents(Canvas canvas) {
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>()
        {

            @Override
            public void handle(MouseEvent event) {
                // context.strokeLine(x, y, event.getX(), event.getY());
                // x = event.getX();
                // y = event.getY();
                player.setPosition(event.getX());
            }

        });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				ball.initVelocity();
			}
        });
    }

    public void startTimer() {
        var timer = new AnimationTimer() {
            long startTime = new Date().getTime();
            long minTime = 1000/60;
            long pastTimeSum = 0;

            @Override
            public void handle(long now) {
                var endTime = new Date().getTime();
                var diffTime = endTime - startTime;
                startTime = endTime;
                pastTimeSum += diffTime;
                while(pastTimeSum > minTime){
                    pastTimeSum -= minTime;
                    update();
                }
                draw();
            }
        };
        timer.start();
    }

    static byte[] toBytes(Color color) {
        return new byte[] { (byte) Math.round(color.getRed() * 255.0), (byte) Math.round(color.getGreen() * 255.0),
                (byte) Math.round(color.getBlue() * 255.0) };
    }

    static byte[] replaceAt(byte[] data, int index, byte[] replacer) {
        for (int i = 0; i < replacer.length; ++i) {
            data[index + i] = replacer[i];
        }
        return data;
    }

    public void dot(Point2D position, Color color) {
        dot(position, 1, color);
    }

    public void dot(Point2D position, int size, Color color) {
        rect(position, new Point2D(size, size), color);
    }

    public class ColorRectID {
        public Color color = null;
        public Point2D size = null;

		public ColorRectID(Color color, Point2D size) {
            this.color = color;
            this.size = size;
		}

        @Override
        public int hashCode() {
            return color.hashCode() + size.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof ColorRectID){
                var crID = (ColorRectID)o;
                var sameColor = crID.color.equals(color);
                var sameSize = crID.size.equals(size);
                // if(sameColor && sameSize){
                //     System.out.println(String.format("SAME %s", this));
                // }else if(sameColor || sameSize){
                //     System.out.println(String.format("PARTSAME %s %s", this, crID));
                // } else {
                //     System.out.println(String.format("NOT %s %s", this, crID));
                // }
                return sameColor && sameSize;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s %s", color, size);
        }
    }
    public class ColorRect{
        public byte[] buffer = null;
        public ColorRectID id = null;
        static public final int colorSize = 4;
        ColorRect(Color color, Point2D size) {
            this(new ColorRectID(color, size));
        }
        ColorRect(ColorRectID id) {
            this.id = id;
            this.buffer = new byte[(int)(colorSize*id.size.getX()*id.size.getY())];

            var r = (byte)Math.round(id.color.getRed()*255);
            var g = (byte)Math.round(id.color.getGreen()*255);
            var b = (byte)Math.round(id.color.getBlue()*255);
            for(int i=0; i<buffer.length; i+=colorSize){
                buffer[i] = b;
                buffer[i+1] = g;
                buffer[i+2] = r;
                buffer[i+3] = (byte)255;
            }
        }
    }
    private HashMap<ColorRectID, ColorRect> rects = new HashMap<ColorRectID, ColorRect>();
    // private PixelFormat format = PixelFormat.getByteBgraPreInstance();

    public void rect(Point2D position, Point2D size, Color color) {
        // var writer = context.getPixelWriter();
        // System.out.println(String.format("%s", writer.getPixelFormat()));

        var bufferID = new ColorRectID(color, size);
        var buffer = rects.get(bufferID);
        if(buffer == null){
            // System.out.println(String.format("%s", bufferID));
            // System.out.print(String.format("ADD."));
            buffer = new ColorRect(bufferID);
            rects.put(buffer.id, buffer);
        }
            // System.out.println(String.format("%s", rects.size()));
        context.setFill(buffer.id.color);
        context.fillRect(
            (int)position.getX(), (int)position.getY(),
            (int)buffer.id.size.getX(), (int)buffer.id.size.getY()
        );
        // writer.setPixels(
        //     (int)position.getX(), (int)position.getY(),
        //     (int)size.getX(), (int)size.getY(),
        //     format,
        //     buffer.buffer, 0, (int)(size.getX()*ColorRect.colorSize*Byte.BYTES));
    }

    public static int byteToInt(byte b) {
        return (b<0) ? 256+b : b;
    }

    public void print(byte[] buffer, int w, int h, int byteSize){
        IntStream.range(0, h)
        .mapToObj(ih ->
            IntStream.range(0, w)
            .mapToObj(iw -> String.format("0x%1$06x",
                IntStream.range(0, byteSize)
                .map(i ->
                    byteToInt(buffer[iw*byteSize+ih*w*byteSize+i]) << 8*(byteSize-i-1)
                )
                .reduce(0x0, (a, b) -> a | b)
            )).toArray()
        ).forEach(a->System.out.println(Arrays.deepToString(a)));

    }

    private void sampleDraw() {
        var pixel = context.getPixelWriter();
        pixel.setColor(100, 100, Color.rgb(0, 0, 255));

        final var dimension = 30;
        var format = PixelFormat.getByteRgbInstance();
        byte[] buffer = new byte[3 * dimension * dimension];

        replaceAt(buffer, 0, toBytes(Color.CYAN));
        replaceAt(buffer, 3 * 9, toBytes(Color.CYAN));
        replaceAt(buffer, 3 * 11, toBytes(Color.CYAN));
        replaceAt(buffer, 3 * 27, toBytes(Color.CYAN));
        replaceAt(buffer, dimension*3 + 0, toBytes(Color.CYAN));
        replaceAt(buffer, dimension*3 + 3 * 9, toBytes(Color.CYAN));
        replaceAt(buffer, dimension*3 + 3 * 11, toBytes(Color.CYAN));
        replaceAt(buffer, dimension*3 + 3 * 27, toBytes(Color.CYAN));

        print(buffer, dimension, dimension, 3);

        int offset = 0;
        int stride = dimension * 3 * Byte.BYTES;

        pixel.setPixels(100, 100, dimension, dimension, format, buffer, offset, stride);
        // context.setStroke(Color.CYAN);
        // context.setLineWidth(12);
        // context.strokeLine(0, 480, 10, 0);
        // context.strokeLine(10, 0, 640, 480);
    }

    public void update() {
        ball.update();
    }

    public void draw() {
        context.clearRect(0, 0, screen.getX(), screen.getY());
        Arrays.stream(stones)
        .forEach(stone -> stone.draw());
        dot(new Point2D(111, 111), 10, Color.LIGHTPINK);
        player.draw();
        ball.draw();
    }
}
