import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import javax.imageio.*;
import java.io.*;

class Figure implements Serializable { // 基底となるクラス
    protected int x, y, width, height, linewidth, originalY;
    protected Color color;

    public Figure(int x, int y, int w, int h, Color c, int l) {
        this.x = x;
        this.y = y;
        this.originalY = y;
        width = w;
        height = h;
        linewidth = l;
        color = c;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setYLocation(int dy) {
        this.y += dy;
    }

    public int getYLocation() {
        return this.y;
    }

    public int getOriginalYLocation() {
        return this.originalY;
    }

    public void reshape(int x1, int y1, int x2, int y2) {
        int newx = Math.min(x1, x2);
        int newy = Math.min(y1, y2);
        int neww = Math.abs(x1 - x2);
        int newh = Math.abs(y1 - y2);
        setLocation(newx, newy);
        setSize(neww, newh);
    }

    public void draw(Graphics g) { // 図形を描く
    }
}

class RectangleFigure extends Figure { // 四角形
    public RectangleFigure(int x, int y, int w, int h, Color c, int l) {
        super(x, y, w, h, c, l);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(linewidth));
        g.drawRect(x, y, width, height);
    }
}

class FillRectangleFigure extends Figure { // 四角形(塗りつぶし)
    public FillRectangleFigure(int x, int y, int w, int h, Color c, int l) {
        super(x, y, w, h, c, l);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(linewidth));
        g2.fillRect(x, y, width, height);
    }
}

class OvalFigure extends Figure { // 楕円
    public OvalFigure(int x, int y, int w, int h, Color c, int l) {
        super(x, y, w, h, c, l);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(linewidth));
        g2.drawOval(x, y, width, height);
    }
}

class FillOvalFigure extends Figure { // 楕円(塗りつぶし)
    public FillOvalFigure(int x, int y, int w, int h, Color c, int l) {
        super(x, y, w, h, c, l);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(linewidth));
        g2.fillOval(x, y, width, height);
    }
}

class LineFigure extends Figure { // 直線
    public LineFigure(int x, int y, int w, int h, Color c, int l) {
        super(x, y, w, h, c, l);
    }

    public void reshape(int x1, int y1, int x2, int y2) {
        int newx = x1;
        int newy = y1;
        int neww = x2 - x1;
        int newh = y2 - y1;
        setLocation(newx, newy);
        setSize(neww, newh);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(linewidth));
        g2.drawLine(x, y, x + width, y + height);
    }
}

class StrokeFigure extends Figure { // 自由描画
    protected ArrayList<Integer> strokeHistory;

    public StrokeFigure(int x, int y, int w, int h, Color c, int l) {
        super(x, y, w, h, c, l);
        strokeHistory = new ArrayList<Integer>();
        strokeHistory.add(x);
        strokeHistory.add(y);
    }

    public void reshape(int x1, int y1, int x2, int y2) {
        strokeHistory.add(x2);
        strokeHistory.add(y2);
    }

    public void setYLocation(int dy) {
        for (int i = 1; i < strokeHistory.size(); i += 2) {
            int y = strokeHistory.get(i);
            strokeHistory.set(i, y + dy);
        }
        this.y += dy;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(linewidth));
        for (int i = 2; i < this.strokeHistory.size(); i += 2) {
            g2.drawLine(strokeHistory.get(i - 2), strokeHistory.get(i - 1),
                strokeHistory.get(i), strokeHistory.get(i + 1));
        }
    }
}

class DrawModel extends Observable { // まとめるクラス
    protected ArrayList<Figure> fig;
    protected Figure drawingFigure;
    protected Color currentColor;
    protected int currentLinewidth;
    protected String currentFigure;
    protected int cnt = 0, tmpCnt = 0, maxCnt = 0;

    public DrawModel() {
        fig = new ArrayList<Figure>();
        drawingFigure = null;
        currentColor = new Color(0, 0, 0);
        currentLinewidth = 1;
        currentFigure = "Rectangle";
    }

    public ArrayList<Figure> getFigures() {
        return fig;
    }

    public Figure getFigure(int idx) {
        return fig.get(idx);
    }

    public void createFigure(int x, int y) { // インスタンス化
        Figure f = null;
        if (this.currentFigure == "Rectangle")
            f = new RectangleFigure(x, y, 0, 0, currentColor, currentLinewidth);
        if (this.currentFigure == "fillRectangle")
            f = new FillRectangleFigure(x, y, 0, 0, currentColor, currentLinewidth);
        if (this.currentFigure == "Oval")
            f = new OvalFigure(x, y, 0, 0, currentColor, currentLinewidth);
        if (this.currentFigure == "fillOval")
            f = new FillOvalFigure(x, y, 0, 0, currentColor, currentLinewidth);
        if (this.currentFigure == "Line")
            f = new LineFigure(x, y, 0, 0, currentColor, currentLinewidth);
        if (this.currentFigure == "Stroke")
            f = new StrokeFigure(x, y, 0, 0, currentColor, currentLinewidth);
        fig.add(this.cnt, f);
        this.cnt += 1;
        this.tmpCnt = this.cnt;
        this.maxCnt = this.cnt;
        drawingFigure = f;
        setChanged();
        notifyObservers();
    }

    public void reshapeFigure(int x1, int y1, int x2, int y2) {
        if (drawingFigure != null) {
            drawingFigure.reshape(x1, y1, x2, y2);
            setChanged();
            notifyObservers();
        }
    }

    public int getCnt() {
        return this.cnt;
    }

    public int getTmpCnt() {
        return this.tmpCnt;
    }

    public int getMaxCnt() {
        return this.maxCnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public void setTmpCnt(int tmpCnt) {
        this.tmpCnt = tmpCnt;
    }

    public void setMaxCnt(int maxCnt) {
        this.maxCnt = maxCnt;
    }
}

class ViewPanel extends JPanel implements Observer { // 実際に表示されるクラス
    protected DrawModel model;

    public ViewPanel(DrawModel m, DrawController c) {
        this.setBackground(Color.white);
        this.addMouseListener(c);
        this.addMouseMotionListener(c);
        model = m;
        model.addObserver(this);
    }

    public void paintComponent(Graphics g) { // ここでdrawを行う
        super.paintComponent(g);
        ArrayList<Figure> fig = model.getFigures();
        for (int i = 0; i < this.model.cnt; i++) {
            Figure f = fig.get(i);
            f.draw(g);
        }
    }

    public void update(Observable o, Object arg) {
        repaint();
    }
}

class SavedObj implements Serializable { // ストロークの保存
    ArrayList<Figure> fig;
    int cnt, maxCnt;

    SavedObj(ArrayList<Figure> fig, int cnt, int maxCnt) {
        this.fig = fig;
        this.cnt = cnt;
        this.maxCnt = maxCnt;
    }
}

class SavePngHistory { // ストロークと画像の保存
    protected DrawModel model;
    protected ViewPanel view;

    public SavePngHistory(DrawModel m, ViewPanel v) {
        this.model = m;
        this.view = v;
    }

    public void save() {
        Dimension rv = this.view.getSize();
        BufferedImage saveImage = new BufferedImage(rv.width, rv.height,
            BufferedImage.TYPE_3BYTE_BGR);
        File output = new File("output.png");
        Graphics2D g2 = saveImage.createGraphics();
        ArrayList<Figure> fig = model.getFigures();
        SavedObj obj = new SavedObj(fig, model.cnt, model.maxCnt);
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream("history.obj"));
            out.writeObject(obj);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, rv.width, rv.height);
        for (int i = 0; i < this.model.cnt; i++) {
            Figure f = fig.get(i);
            f.draw(g2);
        }
        try {
            ImageIO.write(saveImage, "png", output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ColorSelectPanel extends JPanel implements ChangeListener, ActionListener { // 色選択

    JPanel redP, greenP, blueP, currentColorP, chooserAndCurrentColorP, allP;
    JSlider redSlider, greenSlider, blueSlider;
    JLabel redLabel, greenLabel, blueLabel;
    JButton chooserB;
    JColorChooser chooser;
    DrawModel model;

    public ColorSelectPanel(DrawModel model) {
        this.model = model;

        redP = new JPanel();
        redSlider = new JSlider(0, 255, 0);
        redSlider.addChangeListener(this);
        redLabel = new JLabel("0");
        redP.add(new JLabel("RED"), BorderLayout.WEST);
        redP.add(redSlider, BorderLayout.CENTER);
        redP.add(redLabel, BorderLayout.EAST);

        greenP = new JPanel();
        greenSlider = new JSlider(0, 255, 0);
        greenSlider.addChangeListener(this);
        greenLabel = new JLabel("0");
        greenP.add(new JLabel("GREEN"), BorderLayout.WEST);
        greenP.add(greenSlider, BorderLayout.CENTER);
        greenP.add(greenLabel, BorderLayout.EAST);

        blueP = new JPanel();
        blueSlider = new JSlider(0, 255, 0);
        blueSlider.addChangeListener(this);
        blueLabel = new JLabel("0");
        blueP.add(new JLabel("BLUE"), BorderLayout.WEST);
        blueP.add(blueSlider, BorderLayout.CENTER);
        blueP.add(blueLabel, BorderLayout.EAST);

        chooserB = new JButton("ColorChooser");
        chooserB.addActionListener(this);

        currentColorP = new JPanel();
        currentColorP.setBackground(getCurrentColor());

        chooserAndCurrentColorP = new JPanel();
        chooserAndCurrentColorP.add(chooserB);
        chooserAndCurrentColorP.add(currentColorP);

        allP = new JPanel();
        allP.setLayout(new GridLayout(4, 1));
        allP.add(redP);
        allP.add(greenP);
        allP.add(blueP);
        allP.add(chooserAndCurrentColorP);
        this.add(allP);
    }

    public Color getCurrentColor() { // 現在の色を取得
        return new Color(redSlider.getValue(), greenSlider.getValue(),
            blueSlider.getValue());
    }

    public void stateChanged(ChangeEvent e) { // スライダーが変更された場合
        if (e.getSource() == redSlider) {
            int value = redSlider.getValue();
            redLabel.setText(String.valueOf(value));
        }
        if (e.getSource() == greenSlider) {
            int value = greenSlider.getValue();
            greenLabel.setText(String.valueOf(value));
        }
        if (e.getSource() == blueSlider) {
            int value = blueSlider.getValue();
            blueLabel.setText(String.valueOf(value));
        }
        this.model.currentColor = getCurrentColor();
        this.currentColorP.setBackground(getCurrentColor());
    }

    public void actionPerformed(ActionEvent e) { // ボタンが押された場合
        JColorChooser colorchooser = new JColorChooser();
        Color color = colorchooser.showDialog(this, "Choose Color", Color.white);
        this.model.currentColor = color;
        this.redSlider.setValue(color.getRed());
        this.greenSlider.setValue(color.getGreen());
        this.blueSlider.setValue(color.getBlue());
        this.currentColorP.setBackground(getCurrentColor());
    }
}

class ShapeSelectPanel extends JPanel implements ChangeListener, ActionListener { // 図形選択
    JButton rectangleB, fillRectangleB, ovalB, fillOvalB, lineB, strokeB;
    JSlider linewidthSlider;
    JLabel linewidthLabel;
    JPanel linewidthPanel;
    DrawModel model;

    public ShapeSelectPanel(DrawModel model) {
        this.model = model;
        rectangleB = new JButton("Rectangle");
        fillRectangleB = new JButton("fillRectangle");
        ovalB = new JButton("Oval");
        fillOvalB = new JButton("fillOval");
        lineB = new JButton("Line");
        strokeB = new JButton("Stroke");

        rectangleB.addActionListener(this);
        fillRectangleB.addActionListener(this);
        ovalB.addActionListener(this);
        fillOvalB.addActionListener(this);
        lineB.addActionListener(this);
        strokeB.addActionListener(this);

        linewidthPanel = new JPanel();
        linewidthSlider = new JSlider(1, 40, 1);
        linewidthSlider.addChangeListener(this);
        linewidthLabel = new JLabel("1");
        linewidthPanel.add(new JLabel("linewidth"), BorderLayout.WEST);
        linewidthPanel.add(linewidthSlider, BorderLayout.CENTER);
        linewidthPanel.add(linewidthLabel, BorderLayout.EAST);

        this.add(rectangleB);
        this.add(fillRectangleB);
        this.add(ovalB);
        this.add(fillOvalB);
        this.add(lineB);
        this.add(strokeB);
        this.add(linewidthPanel, BorderLayout.CENTER);
    }

    public void stateChanged(ChangeEvent e) { // スライダーが変更された場合
        int value = linewidthSlider.getValue();
        linewidthLabel.setText(String.valueOf(value));
        this.model.currentLinewidth = value;
    }

    public void actionPerformed(ActionEvent e) { // ボタンが押された場合
        if (e.getSource() == rectangleB)
            model.currentFigure = "Rectangle";
        if (e.getSource() == fillRectangleB)
            model.currentFigure = "fillRectangle";
        if (e.getSource() == ovalB)
            model.currentFigure = "Oval";
        if (e.getSource() == fillOvalB)
            model.currentFigure = "fillOval";
        if (e.getSource() == lineB)
            model.currentFigure = "Line";
        if (e.getSource() == strokeB)
            model.currentFigure = "Stroke";
    }
}

class PredictPanel extends JPanel implements ActionListener { // 数字予測
    JLabel predictLabel, predictProbaLabel, predictNoteLabel;
    JButton predictB;
    DrawModel model;
    ViewPanel view;
    protected double[][] weight;

    public PredictPanel(DrawModel model, ViewPanel view) { // weight.txtを読み込む
        this.model = model;
        this.view = view;
        predictB = new JButton("Predict");
        predictB.addActionListener(this);
        predictLabel = new JLabel("Digit: -");
        predictProbaLabel = new JLabel("Probability: -");
        predictNoteLabel = new JLabel("※ウィンドウを小さくしてください");
        this.setLayout(new GridLayout(4, 1));
        this.add(predictB);
        this.add(predictLabel);
        this.add(predictProbaLabel);
        this.add(predictNoteLabel);

        this.weight = new double[10][28 * 28 + 1];
        BufferedReader br = null;
        try {
            File file = new File("weight.txt");
            br = new BufferedReader(new FileReader(file));
            String line;
            String[] data;
            int i = 0;
            while ((line = br.readLine()) != null) {
                data = line.split(",");
                for (int j = 0; j < data.length; ++j)
                    weight[i][j] = Double.parseDouble(data[j]);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void actionPerformed(ActionEvent e) { // ボタンが押された場合予測を行いJLabelを変更する
        Dimension rv = this.view.getSize();
        BufferedImage originalImg = new BufferedImage(rv.width, rv.height,
            BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2 = originalImg.createGraphics();
        ArrayList<Figure> fig = model.getFigures();
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, rv.width, rv.height);
        for (int i = 0; i < this.model.cnt; i++) {
            Figure f = fig.get(i);
            f.draw(g2);
        }
        BufferedImage resizedImg = new BufferedImage(28, 28, BufferedImage.TYPE_3BYTE_BGR);
        resizedImg.createGraphics().drawImage(originalImg.getScaledInstance(28, 28,
            Image.SCALE_AREA_AVERAGING), 0, 0, 28, 28, null);
        double[] imgArr = new double[28 * 28];
        for (int i = 0; i < 28 * 28; ++i) {
            Color c = new Color(resizedImg.getRGB(i % 28, i / 28));
            imgArr[i] = c.getRed() + c.getGreen() + c.getBlue() == 255 * 3 ? 0.0 : 1.0;
        }
        double[] prob = new double[10];
        double softmaxDenominator = 0.0;
        int maxIdx = 0;
        for (int i = 0; i < 10; ++i) {
            double s = weight[i][0];
            for (int j = 0; j < 28 * 28; ++j) {
                s += imgArr[j] * weight[i][j + 1];
            }
            prob[i] = s;
            maxIdx = prob[i] > prob[maxIdx] ? i : maxIdx;
        }
        double minus = prob[maxIdx];
        for (int i = 0; i < 10; ++i) {
            prob[i] -= minus;
            softmaxDenominator += Math.exp(prob[i]);
        }
        double resultProb = Math.exp(prob[maxIdx]) / softmaxDenominator;
        predictLabel.setText("Digit: " + String.valueOf(maxIdx));
        predictProbaLabel.setText("Probability: " +
            String.valueOf((int) (resultProb * 100)) + "%");
    }
}

class UndoAnimeThread extends Thread { // Undoをした場合のアニメーション
    DrawModel model;
    Figure f;
    int maxY;
    ViewPanel view;

    public UndoAnimeThread(DrawModel model, Figure f, ViewPanel view) {
        this.model = model;
        this.f = f;
        this.view = view;
        Dimension rv = this.view.getSize();
        this.maxY = f.getOriginalYLocation() + rv.height;
    }

    public void run() { // Figureのy座標を変化させる
        while (this.f.getYLocation() < this.maxY) {
            this.f.setYLocation(3);
            this.view.repaint();
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
        this.model.setCnt(this.model.getCnt() - 1);
    }
}

class RedoAnimeThread extends Thread { // Redoをした場合のアニメーション
    DrawModel model;
    Figure f;
    int maxY;
    ViewPanel view;

    public RedoAnimeThread(DrawModel model, Figure f, ViewPanel view) {
        this.model = model;
        this.f = f;
        this.view = view;
    }

    public void run() { // Figureのy座標を変化させる
        this.model.setCnt(this.model.getCnt() + 1);
        while (this.f.getYLocation() > this.f.getOriginalYLocation()) {
            this.f.setYLocation(-3);
            this.view.repaint();
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
    }
}

class UndoRedoSaveLoadPanel extends JPanel implements ActionListener {  // Undoなどをパネルにする
    JButton undoB, redoB, saveB, loadB;
    SavePngHistory save;
    DrawModel model;
    ViewPanel view;

    public UndoRedoSaveLoadPanel(DrawModel model, ViewPanel view) {
        this.save = new SavePngHistory(model, view);
        this.model = model;
        this.view = view;

        undoB = new JButton("Undo");
        undoB.addActionListener(this);
        this.add(undoB);

        redoB = new JButton("Redo");
        redoB.addActionListener(this);
        this.add(redoB);

        saveB = new JButton("Save");
        saveB.addActionListener(this);
        this.add(saveB);

        loadB = new JButton("Load");
        loadB.addActionListener(this);
        this.add(loadB);
    }

    public void actionPerformed(ActionEvent e) {
        int tmpCnt = this.model.getTmpCnt();
        if (e.getSource() == undoB) {
            if (0 < tmpCnt) {
                this.model.setTmpCnt(tmpCnt - 1);
                Figure f = this.model.getFigure(tmpCnt - 1);
                UndoAnimeThread undoAnime = new UndoAnimeThread(this.model, f, this.view);
                undoAnime.start();
            }
        }

        if (e.getSource() == redoB) {
            if (tmpCnt < this.model.getMaxCnt()) {
                this.model.setTmpCnt(tmpCnt + 1);
                Figure f = this.model.getFigure(tmpCnt);
                RedoAnimeThread redoAnime = new RedoAnimeThread(this.model, f, view);
                redoAnime.start();
            }
        }

        if (e.getSource() == saveB) {
            this.save.save();
            this.view.repaint();
        }

        if (e.getSource() == loadB) {
            try (ObjectInputStream in = new ObjectInputStream(new
                FileInputStream("history.obj"))) {
                SavedObj obj = (SavedObj) in.readObject();
                this.model.fig = obj.fig;
                this.model.setCnt(obj.cnt);
                this.model.setTmpCnt(obj.cnt);
                this.model.setMaxCnt(obj.maxCnt);
            } catch (Exception err) {
                err.printStackTrace();
            }
            this.view.repaint();
        }
    }
}

class DrawFrame extends JFrame {  // 全て表示する
    DrawModel model;
    ViewPanel view;
    ColorSelectPanel colorSelect;
    ShapeSelectPanel shapeSelect;
    PredictPanel predict;
    UndoRedoSaveLoadPanel undoRedoSaveLoad;
    DrawController cont;
    JTabbedPane tabbedpane;
    SavePngHistory save;

    public DrawFrame() {
        model = new DrawModel();
        cont = new DrawController(model);
        view = new ViewPanel(model, cont);
        colorSelect = new ColorSelectPanel(model);
        shapeSelect = new ShapeSelectPanel(model);
        predict = new PredictPanel(model, view);
        save = new SavePngHistory(model, view);
        undoRedoSaveLoad = new UndoRedoSaveLoadPanel(model, view);
        tabbedpane = new JTabbedPane();
        tabbedpane.addTab("Color", colorSelect);
        tabbedpane.addTab("Shape", shapeSelect);
        tabbedpane.addTab("Predict", predict);
        tabbedpane.setSelectedIndex(0);

        this.setBackground(Color.black);
        this.setTitle("Draw Editor");
        this.setSize(750, 750);
        this.add(tabbedpane, BorderLayout.NORTH);
        this.add(view);
        this.add(undoRedoSaveLoad, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new DrawFrame();
    }
}

class DrawController implements MouseListener, MouseMotionListener {  // マウスが押された場合などにmodelのメソッドを呼ぶ
    protected DrawModel model;
    protected int dragStartX, dragStartY;

    public DrawController(DrawModel a) {
        model = a;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        dragStartX = e.getX();
        dragStartY = e.getY();
        model.createFigure(dragStartX, dragStartY);
    }

    public void mouseDragged(MouseEvent e) {
        model.reshapeFigure(dragStartX, dragStartY, e.getX(), e.getY());
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
}
