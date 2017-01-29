/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemacontrole;
//import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import static java.lang.Math.ceil;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.swing.JOptionPane;
import static javax.swing.text.StyleConstants.FontFamily;
//import javax.swing.text.Document;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
//import javax.swing.text.Document;



/**
 *
 * @author jaime
 */
public class MainWindow extends javax.swing.JFrame {
    int registro=1;
    ImageIcon iconON = new ImageIcon(SistemaControle.class.getResource("/imagens/diagrama_on.png"));
    ImageIcon iconOFF = new ImageIcon(SistemaControle.class.getResource("/imagens/diagrama_off.png"));
    ImageIcon iconRV = new ImageIcon(SistemaControle.class.getResource("/imagens/diagrama_reverse.png"));
    GerarPDFWindow TelaPDF;
    DefaultTableModel model;
    TableColumnModel colunas, colunasAUX;
    TableColumn col;
    List<TableColumn> colunasRemovidas;
    boolean cascata = false;
    String kp_t1="";
    String ki_t1="";
    String kd_t1="";
    String ti_t1="";
    String td_t1="";
    String controleT1 ="";
    int tamColunas[] = new int[9];
    PdfPTable pdfTableCascata;
    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        TelaPDF = new GerarPDFWindow();
        TelaPDF.addGerarFuncaoListener(new GerarPDF());
        TelaPDF.pack();
        TelaPDF.setLocationRelativeTo(null);
        TelaPDF.setTitle("Criação de Relatório");
        TelaPDF.setDefaultCloseOperation(TelaPDF.DISPOSE_ON_CLOSE);
        model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
               //all cells false
               return false;
            }
        };
        pdfTableCascata = new PdfPTable(6);
        Font fontTITULO = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
                fontTITULO.setColor(BaseColor.RED);
                   Paragraph tipo = new Paragraph("\n" +
                    "Controle" ,fontTITULO);
                   pdfTableCascata.addCell(tipo);
                   Paragraph KP = new Paragraph("\n" +
                    "Kp" ,fontTITULO);
                   pdfTableCascata.addCell(KP);
                   Paragraph KD = new Paragraph("\n" +
                    "Kd" ,fontTITULO);
                   pdfTableCascata.addCell(KD);
                   Paragraph KI = new Paragraph("\n" +
                    "Ki" ,fontTITULO);
                   pdfTableCascata.addCell(KI);
                   Paragraph TI = new Paragraph("\n" +
                    "Ti" ,fontTITULO);
                   pdfTableCascata.addCell(TI);
                   Paragraph TD = new Paragraph("\n" +
                    "Td" ,fontTITULO);
                   pdfTableCascata.addCell(TD);

        
        initComponents();
        
        model.addColumn("SetPoint");
        model.addColumn("Tpico");
        model.addColumn("Tr 100%");
        model.addColumn("Tr 95%");
        model.addColumn("Tr 90%");
        model.addColumn("Ts 2%");
        model.addColumn("Ts 5%");
        model.addColumn("Ts 10%");
        model.addColumn("Mp% Mp");
        model.addColumn("Kp");
        model.addColumn("Ki");
        model.addColumn("Ti");
        model.addColumn("Kd");
        model.addColumn("Td");
        model.addColumn("Control");

        
        this.colunas = this.tabelaDetalhes.getColumnModel();
        for(int i=0; i<15; i++){
            this.colunas.getColumn(i).setMinWidth(0);
        }
        this.colunas.getColumn(3).setMaxWidth(0);
        this.colunas.getColumn(4).setMaxWidth(0);
        this.colunas.getColumn(5).setMaxWidth(0);
        this.colunas.getColumn(6).setMaxWidth(0);
        this.colunas.getColumn(9).setMaxWidth(0);
        this.colunas.getColumn(10).setMaxWidth(0);
        this.colunas.getColumn(11).setMaxWidth(0);
        this.colunas.getColumn(12).setMaxWidth(0);
        this.colunas.getColumn(13).setMaxWidth(0);
        this.colunas.getColumn(14).setMaxWidth(0);

        //Object ob = new Object[] { "0-10cm"};
        //addNewRow();
        //setValueTable("SetPoint","0-10cm");
//        model.addRow(new Object[] { "0-10cm", "5s", "3s", "20s", "10s", "14s", "40s", "30s", "20% 2cm" });
        //this.tabelaDetalhes.setValueAt("15s", this.tabelaDetalhes.getRowCount()-1, 7);
    }
    
    //objeto model que vai ser usado para adicionar dinamicamente linhas a tabela
    public DefaultTableModel getTableModel(){
        return this.model;
    }
    
    public void addNewRow(){
        this.model.addRow(new Object[]{});
    }
    
    public void setValueTable(String type, String value){
        int column = 0;
        switch(type){
            case "SetPoint":
                column = 0;
                break;
            case "Tpico":
                column = 1;
                break;
            case "Tr100":
                column = 2;
                break;
            case "Tr95":
                column = 3;
                break;
            case "Tr90":
                column = 4;
                break;
            case "Ts2":
                column = 5;
                break;
            case "Ts5":
                column = 6;
                break;
            case "Ts10":
                column = 7;
                break;
            case "Mp":
                column = 8;
                break;
            case "Kp":
                column = 9;
                break;
            case "Ki":
                column = 10;
                break;
            case "Ti":
                column = 11;
                break;
            case "Kd":
                column = 12;
                break;
            case "Td":
                column = 13;
                break;
            case "Control":
                column = 14;
                break;
            default:
                break;
        }
        this.tabelaDetalhes.setValueAt(value, this.tabelaDetalhes.getRowCount()-1, column);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ControleSelecao = new javax.swing.ButtonGroup();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        PainelEntrada = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        SaidaCanal0 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        SaidaCanal1 = new javax.swing.JCheckBox();
        SaidaCanal2 = new javax.swing.JCheckBox();
        SaidaCanal3 = new javax.swing.JCheckBox();
        SaidaCanal4 = new javax.swing.JCheckBox();
        SaidaCanal5 = new javax.swing.JCheckBox();
        SaidaCanal6 = new javax.swing.JCheckBox();
        BotaoLer = new javax.swing.JToggleButton();
        nivelObs1ChBox = new javax.swing.JCheckBox();
        nivelObs2ChBox = new javax.swing.JCheckBox();
        PainelSaida = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        BotaoTipoDeFuncao = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        pararSinalBt = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        tanque1Progress = new javax.swing.JProgressBar();
        tanque2Progress = new javax.swing.JProgressBar();
        LabelDiagrama = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaDetalhes = new javax.swing.JTable();
        tabelaTr100 = new javax.swing.JCheckBox();
        tabelaTr95 = new javax.swing.JCheckBox();
        tabelaTr90 = new javax.swing.JCheckBox();
        tabelaTpico = new javax.swing.JCheckBox();
        tabelaTs2 = new javax.swing.JCheckBox();
        tabelaTs5 = new javax.swing.JCheckBox();
        tabelaKp = new javax.swing.JCheckBox();
        tabelaKi = new javax.swing.JCheckBox();
        tabelaTi = new javax.swing.JCheckBox();
        tabelaKd = new javax.swing.JCheckBox();
        tabelaTs10 = new javax.swing.JCheckBox();
        tabelaMp = new javax.swing.JCheckBox();
        tabelaSetPoint = new javax.swing.JCheckBox();
        tabelaTd = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        logMenuItem = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        jMenuItem2.setText("jMenuItem2");

        jMenuItem4.setText("jMenuItem4");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1370, 741));
        setPreferredSize(new java.awt.Dimension(1370, 760));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        PainelEntrada.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout PainelEntradaLayout = new javax.swing.GroupLayout(PainelEntrada);
        PainelEntrada.setLayout(PainelEntradaLayout);
        PainelEntradaLayout.setHorizontalGroup(
            PainelEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 653, Short.MAX_VALUE)
        );
        PainelEntradaLayout.setVerticalGroup(
            PainelEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 328, Short.MAX_VALUE)
        );

        getContentPane().add(PainelEntrada, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 16, -1, -1));

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        SaidaCanal0.setSelected(true);
        SaidaCanal0.setText("Canal 0");
        SaidaCanal0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaidaCanal0ActionPerformed(evt);
            }
        });
        jPanel2.add(SaidaCanal0, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 32, -1, -1));

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel1.setText("Variáveis de Saída");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(28, 8, -1, -1));

        SaidaCanal1.setText("Canal 1");
        SaidaCanal1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaidaCanal1ActionPerformed(evt);
            }
        });
        jPanel2.add(SaidaCanal1, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 56, -1, -1));

        SaidaCanal2.setText("Canal 2");
        SaidaCanal2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaidaCanal2ActionPerformed(evt);
            }
        });
        jPanel2.add(SaidaCanal2, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 80, -1, -1));

        SaidaCanal3.setText("Canal 3");
        SaidaCanal3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaidaCanal3ActionPerformed(evt);
            }
        });
        jPanel2.add(SaidaCanal3, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 104, -1, -1));

        SaidaCanal4.setText("Canal 4");
        SaidaCanal4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaidaCanal4ActionPerformed(evt);
            }
        });
        jPanel2.add(SaidaCanal4, new org.netbeans.lib.awtextra.AbsoluteConstraints(74, 32, -1, -1));

        SaidaCanal5.setText("Canal 5");
        SaidaCanal5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaidaCanal5ActionPerformed(evt);
            }
        });
        jPanel2.add(SaidaCanal5, new org.netbeans.lib.awtextra.AbsoluteConstraints(74, 56, -1, -1));

        SaidaCanal6.setText("Canal 6");
        SaidaCanal6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaidaCanal6ActionPerformed(evt);
            }
        });
        jPanel2.add(SaidaCanal6, new org.netbeans.lib.awtextra.AbsoluteConstraints(74, 80, -1, -1));

        BotaoLer.setText("LER");
        BotaoLer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BotaoLerActionPerformed(evt);
            }
        });
        jPanel2.add(BotaoLer, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 190, -1, -1));

        nivelObs1ChBox.setText("Nivel Observado 1");
        jPanel2.add(nivelObs1ChBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 128, -1, -1));

        nivelObs2ChBox.setText("Nivel Observado 2");
        jPanel2.add(nivelObs2ChBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 152, -1, -1));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 10, 160, 230));

        PainelSaida.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout PainelSaidaLayout = new javax.swing.GroupLayout(PainelSaida);
        PainelSaida.setLayout(PainelSaidaLayout);
        PainelSaidaLayout.setHorizontalGroup(
            PainelSaidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 653, Short.MAX_VALUE)
        );
        PainelSaidaLayout.setVerticalGroup(
            PainelSaidaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 328, Short.MAX_VALUE)
        );

        getContentPane().add(PainelSaida, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 388, -1, -1));

        jLabel7.setText("Níveis dos Tanques");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 0, -1, -1));

        jLabel8.setText("Tensão das bombas");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 359, -1, -1));

        BotaoTipoDeFuncao.setText("Tipo de Função");
        getContentPane().add(BotaoTipoDeFuncao, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 260, 180, 40));

        jPanel1.setLayout(new java.awt.GridLayout(1, 0));
        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pararSinalBt.setBackground(new java.awt.Color(255, 255, 255));
        pararSinalBt.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        pararSinalBt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/stop.png"))); // NOI18N
        pararSinalBt.setText("Parar Sinal");
        pararSinalBt.setMaximumSize(new java.awt.Dimension(140, 29));
        pararSinalBt.setPreferredSize(new java.awt.Dimension(140, 42));
        pararSinalBt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pararSinalBtActionPerformed(evt);
            }
        });
        getContentPane().add(pararSinalBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 300, 180, 40));

        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        tanque1Progress.setOrientation(1);
        tanque1Progress.setValue(50);
        tanque1Progress.setMaximumSize(new java.awt.Dimension(12, 10));
        tanque1Progress.setMinimumSize(new java.awt.Dimension(10, 10));
        tanque1Progress.setPreferredSize(new java.awt.Dimension(10, 10));
        tanque1Progress.setSize(new java.awt.Dimension(10, 10));

        tanque2Progress.setOrientation(1);
        tanque2Progress.setValue(50);

        LabelDiagrama.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/diagrama_off.png"))); // NOI18N

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/base.png"))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel3.setText("I");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel4))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(LabelDiagrama, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tanque2Progress, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tanque1Progress, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(tanque1Progress, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(tanque2Progress, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(LabelDiagrama))
                .addGap(0, 0, 0)
                .addComponent(jLabel4)
                .addContainerGap())
        );

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 390, -1, -1));

        tabelaDetalhes.setModel(model);
        jScrollPane2.setViewportView(tabelaDetalhes);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 20, 410, -1));

        tabelaTr100.setSelected(true);
        tabelaTr100.setText("Tr 100%");
        tabelaTr100.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTr100StateChanged(evt);
            }
        });
        tabelaTr100.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabelaTr100ActionPerformed(evt);
            }
        });
        getContentPane().add(tabelaTr100, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 440, -1, -1));

        tabelaTr95.setText("Tr 95%");
        tabelaTr95.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTr95StateChanged(evt);
            }
        });
        getContentPane().add(tabelaTr95, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 440, -1, -1));

        tabelaTr90.setText("Tr 90%");
        tabelaTr90.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTr90StateChanged(evt);
            }
        });
        getContentPane().add(tabelaTr90, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 440, -1, -1));

        tabelaTpico.setSelected(true);
        tabelaTpico.setText("Tpico");
        tabelaTpico.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTpicoStateChanged(evt);
            }
        });
        getContentPane().add(tabelaTpico, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 470, -1, -1));

        tabelaTs2.setText("Ts 2%");
        tabelaTs2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTs2StateChanged(evt);
            }
        });
        getContentPane().add(tabelaTs2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 470, -1, -1));

        tabelaTs5.setText("Ts 5%");
        tabelaTs5.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTs5StateChanged(evt);
            }
        });
        getContentPane().add(tabelaTs5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 470, -1, -1));

        tabelaKp.setText("Kp");
        tabelaKp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaKpStateChanged(evt);
            }
        });
        getContentPane().add(tabelaKp, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 500, -1, -1));

        tabelaKi.setText("Ki");
        tabelaKi.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaKiStateChanged(evt);
            }
        });
        getContentPane().add(tabelaKi, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 500, -1, -1));

        tabelaTi.setText("Ti");
        tabelaTi.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTiStateChanged(evt);
            }
        });
        getContentPane().add(tabelaTi, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 500, -1, -1));

        tabelaKd.setText("Kd");
        tabelaKd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaKdStateChanged(evt);
            }
        });
        getContentPane().add(tabelaKd, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 530, -1, -1));

        tabelaTs10.setSelected(true);
        tabelaTs10.setText("Ts 10%");
        tabelaTs10.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTs10StateChanged(evt);
            }
        });
        getContentPane().add(tabelaTs10, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 470, -1, -1));

        tabelaMp.setSelected(true);
        tabelaMp.setText("Mp% Mp");
        tabelaMp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaMpStateChanged(evt);
            }
        });
        getContentPane().add(tabelaMp, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 500, -1, -1));

        tabelaSetPoint.setSelected(true);
        tabelaSetPoint.setText("SetPoint");
        tabelaSetPoint.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaSetPointStateChanged(evt);
            }
        });
        getContentPane().add(tabelaSetPoint, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 440, -1, -1));

        tabelaTd.setText("Td");
        tabelaTd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabelaTdStateChanged(evt);
            }
        });
        getContentPane().add(tabelaTd, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 530, -1, -1));

        jButton1.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/relatorio.png"))); // NOI18N
        jButton1.setText("Gerar Relatório");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 340, 180, 40));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/media.png"))); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 600, -1, -1));

        jMenu1.setText("TC control");

        jMenuItem1.setText("Sobre");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem5.setText("Ajuda");
        jMenu1.add(jMenuItem5);

        jMenuItem6.setText("Sair");
        jMenu1.add(jMenuItem6);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        logMenuItem.setText("Log");
        logMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logMenuItemActionPerformed(evt);
            }
        });

        jMenuItem3.setText("Abrir log sistema");
        jMenuItem3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuItem3MouseClicked(evt);
            }
        });
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        logMenuItem.add(jMenuItem3);

        jMenuBar1.add(logMenuItem);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void SaidaCanal0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaidaCanal0ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SaidaCanal0ActionPerformed

    private void SaidaCanal1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaidaCanal1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SaidaCanal1ActionPerformed

    private void SaidaCanal2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaidaCanal2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SaidaCanal2ActionPerformed

    private void SaidaCanal3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaidaCanal3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SaidaCanal3ActionPerformed

    private void SaidaCanal4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaidaCanal4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SaidaCanal4ActionPerformed

    private void SaidaCanal5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaidaCanal5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SaidaCanal5ActionPerformed

    private void SaidaCanal6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaidaCanal6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SaidaCanal6ActionPerformed

    private void BotaoLerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BotaoLerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BotaoLerActionPerformed

    private void logMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logMenuItemActionPerformed
        
    }//GEN-LAST:event_logMenuItemActionPerformed

    private void pararSinalBtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pararSinalBtActionPerformed
        //salva os grafico
    BufferedImage img = new BufferedImage(PainelEntrada.getWidth(), PainelEntrada.getHeight(), BufferedImage.TYPE_INT_RGB);
    PainelEntrada.print(img.getGraphics()); // or: panel.printAll(...);
    try {
        ImageIO.write(img, "jpg", new File("./Raw_Data/PainelEntrada"+this.registro+".jpg"));
    }
    catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    
    BufferedImage img2 = new BufferedImage(PainelSaida.getWidth(), PainelSaida.getHeight(), BufferedImage.TYPE_INT_RGB);
    PainelSaida.print(img2.getGraphics()); // or: panel.printAll(...);
    try {
        ImageIO.write(img2, "jpg", new File("./Raw_Data/PainelSaida"+this.registro+".jpg"));
    }
    catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    this.registro++;
    Font fontTITULO2 = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.NORMAL);
                Font fontTITULO = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
                fontTITULO.setColor(BaseColor.RED);

                Font fontDADO = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
                    if(cascata){
                   
                   Paragraph tipo_VALOR = new Paragraph((this.controleT1),fontDADO);
                   Paragraph KP_VALOR = new Paragraph((this.kp_t1),fontDADO);
                   Paragraph KD_VALOR = new Paragraph((this.kd_t1),fontDADO);
                   Paragraph KI_VALOR = new Paragraph((this.ki_t1),fontDADO);
                   Paragraph TI_VALOR = new Paragraph((this.ti_t1),fontDADO);
                   Paragraph TD_VALOR = new Paragraph((this.td_t1),fontDADO);
                   pdfTableCascata.addCell(tipo_VALOR);
                   pdfTableCascata.addCell(KP_VALOR);
                   pdfTableCascata.addCell(KD_VALOR);
                   pdfTableCascata.addCell(KI_VALOR);
                   pdfTableCascata.addCell(TI_VALOR);
                   pdfTableCascata.addCell(TD_VALOR);

                }
        // TODO add your handling code here:
    }//GEN-LAST:event_pararSinalBtActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed

    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem3MouseClicked
        
    }//GEN-LAST:event_jMenuItem3MouseClicked

    private void tabelaTr100ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabelaTr100ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tabelaTr100ActionPerformed

    private void tabelaTr100StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTr100StateChanged
        if(this.tabelaTr100.isSelected()){
            this.colunas.getColumn(2).setMaxWidth(200);
            this.colunas.getColumn(2).setPreferredWidth(75);
        }
        else{
            this.colunas.getColumn(2).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTr100StateChanged

    private void tabelaTr95StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTr95StateChanged
        if(this.tabelaTr95.isSelected()){
            this.colunas.getColumn(3).setMaxWidth(200);
            this.colunas.getColumn(3).setPreferredWidth(75);
        }
        else{
            this.colunas.getColumn(3).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTr95StateChanged

    private void tabelaTr90StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTr90StateChanged
        if(this.tabelaTr90.isSelected()){
            this.colunas.getColumn(4).setMaxWidth(200);
            this.colunas.getColumn(4).setPreferredWidth(75);
        }
        else{
            this.colunas.getColumn(4).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTr90StateChanged

    private void tabelaTs2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTs2StateChanged
        if(this.tabelaTs2.isSelected()){
            this.colunas.getColumn(5).setMaxWidth(200);
            this.colunas.getColumn(5).setPreferredWidth(75);
        }
        else{
            this.colunas.getColumn(5).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTs2StateChanged

    private void tabelaTs5StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTs5StateChanged
        if(this.tabelaTs5.isSelected()){
            this.colunas.getColumn(6).setMaxWidth(200);
            this.colunas.getColumn(6).setPreferredWidth(75);
        }
        else{
            this.colunas.getColumn(6).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTs5StateChanged

    private void tabelaTpicoStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTpicoStateChanged
        if(this.tabelaTpico.isSelected()){
            this.colunas.getColumn(1).setMaxWidth(200);
            this.colunas.getColumn(1).setPreferredWidth(75);
        }
        else{
            this.colunas.getColumn(1).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTpicoStateChanged

    private void tabelaTs10StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTs10StateChanged
        if(this.tabelaTs10.isSelected()){
            this.colunas.getColumn(7).setMaxWidth(200);
            this.colunas.getColumn(7).setPreferredWidth(75);
        }
        else{
            this.colunas.getColumn(7).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTs10StateChanged

    private void tabelaMpStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaMpStateChanged
        if(this.tabelaMp.isSelected()){
            this.colunas.getColumn(8).setMaxWidth(200);
            this.colunas.getColumn(8).setPreferredWidth(85);
        }
        else{
            this.colunas.getColumn(8).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaMpStateChanged

    private void tabelaSetPointStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaSetPointStateChanged
        if(this.tabelaSetPoint.isSelected()){
            this.colunas.getColumn(0).setMaxWidth(200);
            this.colunas.getColumn(0).setPreferredWidth(90);
        }
        else{
            this.colunas.getColumn(0).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaSetPointStateChanged

    private void tabelaKpStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaKpStateChanged
        if(this.tabelaKp.isSelected()){
            this.colunas.getColumn(9).setMaxWidth(200);
            this.colunas.getColumn(9).setPreferredWidth(70);
        }
        else{
            this.colunas.getColumn(9).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaKpStateChanged

    private void tabelaKiStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaKiStateChanged
        if(this.tabelaKi.isSelected()){
            this.colunas.getColumn(10).setMaxWidth(200);
            this.colunas.getColumn(10).setPreferredWidth(70);
        }
        else{
            this.colunas.getColumn(10).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaKiStateChanged

    private void tabelaTiStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTiStateChanged
        if(this.tabelaTi.isSelected()){
            this.colunas.getColumn(11).setMaxWidth(200);
            this.colunas.getColumn(11).setPreferredWidth(70);
        }
        else{
            this.colunas.getColumn(11).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTiStateChanged

    private void tabelaKdStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaKdStateChanged
        if(this.tabelaKd.isSelected()){
            this.colunas.getColumn(12).setMaxWidth(200);
            this.colunas.getColumn(12).setPreferredWidth(70);
        }
        else{
            this.colunas.getColumn(12).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaKdStateChanged

    private void tabelaTdStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabelaTdStateChanged
        if(this.tabelaTd.isSelected()){
            this.colunas.getColumn(13).setMaxWidth(200);
            this.colunas.getColumn(13).setPreferredWidth(70);
        }
        else{
            this.colunas.getColumn(13).setMaxWidth(0);
        }
    }//GEN-LAST:event_tabelaTdStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        TelaPDF.setVisible(true);
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed
    public void addLerListener(ActionListener e){
        this.BotaoLer.addActionListener(e);
    }
    
    public void addLogWindowOpenListener(ActionListener e){
        this.logMenuItem.addActionListener(e);
    }
    
    public void addPararSinalListener(ActionListener e){
        this.pararSinalBt.addActionListener(e);
    }
    
    public void setValoresTanque1(String escolha, String valor){
        cascata = true;
        switch(escolha){
            case "Controle":
                this.controleT1 = valor;
                break;
            case "Kd":
                this.kd_t1 = valor;
                break;
            case "Ki":
                this.ki_t1 = valor;
                break;
            case "Kp":
                this.kp_t1 = valor;
                break;
            case "ti":
                this.ti_t1 = valor;
                break;
           case "td":
                this.td_t1 = valor;
               break;
           default:       
               break;
        }
        System.out.println("Passei");

    }
    class GerarPDF implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                //TelaPDF.setVisible(false);
                GerarPDFFuncao();
                //MainClass();
            } catch (Exception ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void GerarPDFFuncao() throws BadElementException, IOException {
            try {
                //Define as fontes
                Font fontOBS = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL);
                Font fontTITULO2 = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.NORMAL);
                Font fontTITULO = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
                fontTITULO.setColor(BaseColor.RED);

                Font fontDADO = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
                Document doc = new Document(PageSize.A4.rotate());
                
                // adiciona imagem ao pdf
                Image Logotipo = Image.getInstance("./src/Imagens/imagem_pdf.png");
                Logotipo.scalePercent((float) 47.5);
                PdfPTable tabelaGrfico = new PdfPTable(2);
                for(int i=1; i<this.registro;i++){
                    Image LogotipoI = Image.getInstance("./Raw_Data/PainelEntrada"+i+".jpg");
                    Image LogotipoI2 = Image.getInstance("./Raw_Data/PainelSaida"+i+".jpg");
                    tabelaGrfico.addCell(LogotipoI);
                    tabelaGrfico.addCell(LogotipoI2);
                }


                
                
                int count=this.tabelaDetalhes.getRowCount();
                
                PdfWriter.getInstance(doc, new FileOutputStream(TelaPDF.CampoNome.getText()+".pdf"));
                doc.open();
                PdfPTable pdfTable = new PdfPTable(this.tabelaDetalhes.getColumnCount());
                //adding table headers
                for (int i = 0; i < 15; i++) {
                    pdfTable.addCell(new Paragraph(this.tabelaDetalhes.getColumnName(i),fontTITULO));
                }


                PdfPTable pdfTableAuxiliar = new PdfPTable(this.tabelaDetalhes.getColumnCount());

                String[][] valores = new String[15][15];
                for(int i=0;i<15;i++){
                    for(int j=0;j<15;j++){
                        valores[i][j] = "";
                    }
                }

                Object[] obj= null;
                int contador=1;
                for(int i=0;i<count;i++){
                    for(int j=0;j<15;j++){
                        try{
                          System.out.println("Valor: "+tabelaDetalhes.getModel().getValueAt(i, j).toString()+"Contado: "+contador);
                          valores[i][j]=tabelaDetalhes.getModel().getValueAt(i, j).toString();
                          
                        }catch(Exception e){
                            System.out.println("ERRO  DEU  contador: "+contador);
                          contador++;
                        }
                        
                    }

                }

                    for(int i=0;i<count;i++){
                        for(int j=0;j<15;j++){
                        pdfTableAuxiliar.addCell(new Paragraph(valores[i][j], fontDADO));
                        }
                    }






 
                Paragraph title = new Paragraph(
                "Sistema de Controle de Tanques - TC Control" + "\n\n" ,fontTITULO2);
                title.setAlignment(Paragraph.ALIGN_CENTER);
                
                Calendar calendar = new GregorianCalendar();  
                Date trialTime = new Date();  
                calendar.setTime(trialTime);
                Paragraph DIA = new Paragraph("Data: " + calendar.get(Calendar.DATE) + "/" + (calendar.get(Calendar.MONTH)+1) + "/" + calendar.get(Calendar.YEAR));
                Paragraph HORA = new Paragraph("Hora: " + calendar.get(Calendar.HOUR_OF_DAY)+ ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
                DIA.setAlignment(Paragraph.ALIGN_RIGHT);
                HORA.setAlignment(Paragraph.ALIGN_RIGHT);
                Paragraph espaco = new Paragraph("\n");
                Paragraph aviso = new Paragraph("\nGraficos dos Testes ordenados:");
                aviso.setAlignment(Paragraph.ALIGN_CENTER);
                
                Paragraph autores = new Paragraph("\n" +
                "Desenvolvedores: @AlexandeLUZ - @AndersonDIAS - @HigoBESSA - @JaimeDANTAS" ,fontDADO);
                autores.setAlignment(Paragraph.ALIGN_CENTER);
                
                Paragraph obs = new Paragraph("\n" +
                "Observações: "+TelaPDF.observacoes.getText() ,fontOBS);
                obs.setAlignment(Paragraph.ALIGN_CENTER);

                //PARTE DO CASCATA DO TANQUE 1
               
                Paragraph cascataTitulo = new Paragraph("Parâmetros do Tanque 1 (Escravo):  ",fontTITULO2);
                   cascataTitulo.setAlignment(Paragraph.ALIGN_CENTER);
                   
                //PdfPTable pdfTableCascata = new PdfPTable(5);
//                if(cascata){
//                   
//                   Paragraph KP = new Paragraph("\n" +
//                    "Kp" ,fontTITULO);
//                   pdfTableCascata.addCell(KP);
//                   Paragraph KD = new Paragraph("\n" +
//                    "Kd" ,fontTITULO);
//                   pdfTableCascata.addCell(KD);
//                   Paragraph KI = new Paragraph("\n" +
//                    "Ki" ,fontTITULO);
//                   pdfTableCascata.addCell(KI);
//                   Paragraph TI = new Paragraph("\n" +
//                    "Ti" ,fontTITULO);
//                   pdfTableCascata.addCell(TI);
//                   Paragraph TD = new Paragraph("\n" +
//                    "Td" ,fontTITULO);
//                   pdfTableCascata.addCell(TD);
//                   Paragraph KP_VALOR = new Paragraph((this.kp_t1),fontDADO);
//                   Paragraph KD_VALOR = new Paragraph((this.kd_t1),fontDADO);
//                   Paragraph KI_VALOR = new Paragraph((this.ki_t1),fontDADO);
//                   Paragraph TI_VALOR = new Paragraph((this.ti_t1),fontDADO);
//                   Paragraph TD_VALOR = new Paragraph((this.td_t1),fontDADO);
//                   pdfTableCascata.addCell(KP_VALOR);
//                   pdfTableCascata.addCell(KD_VALOR);
//                   pdfTableCascata.addCell(KI_VALOR);
//                   pdfTableCascata.addCell(TI_VALOR);
//                   pdfTableCascata.addCell(TD_VALOR);
//
//                }
                
                
                doc.add(Logotipo);
                doc.add(title);
                doc.add(pdfTable);
                doc.add(pdfTableAuxiliar);
                if(cascata){
                    doc.add(cascataTitulo);
                    doc.add(espaco);
                    doc.add(pdfTableCascata);     
                }
                doc.add(obs);
                doc.add(aviso);
                //doc.add(espaco);
                doc.add(tabelaGrfico);
                doc.add(espaco);
                doc.add(espaco);
                doc.add(DIA);
                doc.add(HORA);
                doc.add(autores);
                
                
                
                doc.close();
                System.out.println("done");
                mostrarAviso("Arquivo criado com sucesso!");
                TelaPDF.setVisible(false);
            } catch (DocumentException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                mostrarErro("Erro ao gerar PDF!");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                mostrarErro("Erro ao gerar PDF!");

            }

    
          
              
    }
    

    
    public void MainClass() throws Exception {


    Document document = new Document();
    PdfWriter writer;

    writer = PdfWriter.getInstance(document, new FileOutputStream("my_jtable_shapes.pdf"));

    // writer = PdfWriter.getInstance(document, new
    // FileOutputStream("my_jtable_fonts.pdf"));

    document.open();
    PdfContentByte cb = writer.getDirectContent();

    PdfTemplate tp = cb.createTemplate(500, 500);
    Graphics2D g2;

    g2 = tp.createGraphicsShapes(500, 500);

    // g2 = tp.createGraphics(500, 500);
    this.tabelaDetalhes.print(g2);
    g2.dispose();
    cb.addTemplate(tp, 30, 300);

    // step 5: we close the document
    document.close();
  }

    
    public Object GetData(JTable table, int row_index, int col_index){
        return table.getModel().getValueAt(row_index, col_index);
    }
    
    
    public boolean isCanal0(){
        return SaidaCanal0.isSelected();
    }
    public boolean isCanal1(){
        return SaidaCanal1.isSelected();
    }
    public boolean isCanal2(){
        return SaidaCanal2.isSelected();
    }
    public boolean isCanal3(){
        return SaidaCanal3.isSelected();
    }
    public boolean isCanal4(){
        return SaidaCanal4.isSelected();
    }
    public boolean isCanal5(){
        return SaidaCanal5.isSelected();
    }
    public boolean isCanal6(){
        return SaidaCanal6.isSelected();
    }
    
    public boolean[] isCanalSelected(){
        boolean[] canaisSelecionados = new boolean[7];
        if(isCanal0())
            canaisSelecionados[0] = true;
        else 
            canaisSelecionados[0] = false;
        
        if(isCanal1())
            canaisSelecionados[1] = true;
        else 
            canaisSelecionados[1] = false;
        
        if(isCanal2())
            canaisSelecionados[2] = true;
        else 
            canaisSelecionados[2] = false;
        
        if(isCanal3())
            canaisSelecionados[3] = true;
        else 
            canaisSelecionados[3] = false;
        
        if(isCanal4())
            canaisSelecionados[4] = true;
        else 
            canaisSelecionados[4] = false;
        
        if(isCanal5())
            canaisSelecionados[5] = true;
        else 
            canaisSelecionados[5] = false;
        
        if(isCanal6())
            canaisSelecionados[6] = true;
        else 
            canaisSelecionados[6] = false;
        
        
        return canaisSelecionados;
    }
    
    public void updateCanal0(double _valor){
        String valor;
        valor = String.valueOf(_valor);
    }
    public void updateCanal1(double _valor){
        String valor;
        valor = String.valueOf(_valor);
    }
    public void mostrarErro(String erro){
        JOptionPane.showMessageDialog(null, erro, "Erro!", JOptionPane.ERROR_MESSAGE);
    }
    public void mostrarAviso(String erro){
        JOptionPane.showMessageDialog(null, erro, "Aviso!", JOptionPane.WARNING_MESSAGE);
    }
    public void addTipoFuncaoListener(ActionListener listen){
        BotaoTipoDeFuncao.addActionListener(listen);
    }
    
    public void setTanque1Progress(double prog){
        int n = (int) prog;
        this.tanque1Progress.setValue(n);
    }
//    public MainWindow(ImageIcon diagrama){
//        LabelDiagrama.setIcon(diagrama);
//    }
    public void setTanque2Progress(double prog){
        int n = (int) prog;
        this.tanque2Progress.setValue(n);
    }
    
    public void AtualizarDiagrama(String tipo){
        if(tipo.equals("ON")){
            this.LabelDiagrama.setIcon(iconON);
            this.LabelDiagrama.repaint();
        }
        else if(tipo.equals("OFF")){
            this.LabelDiagrama.setIcon(iconOFF);
            this.LabelDiagrama.repaint();
        }
        else if(tipo.equals("RV")){
            this.LabelDiagrama.setIcon(iconRV);
            this.LabelDiagrama.repaint();
        }
        
    }
    
    public boolean isNivelObs1Visible(){
        return this.nivelObs1ChBox.isSelected();
    }
    
    public boolean isNivelObs2Visible(){
        return this.nivelObs2ChBox.isSelected();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton BotaoLer;
    private javax.swing.JButton BotaoTipoDeFuncao;
    private javax.swing.ButtonGroup ControleSelecao;
    protected javax.swing.JLabel LabelDiagrama;
    protected javax.swing.JPanel PainelEntrada;
    protected javax.swing.JPanel PainelSaida;
    private javax.swing.JCheckBox SaidaCanal0;
    private javax.swing.JCheckBox SaidaCanal1;
    private javax.swing.JCheckBox SaidaCanal2;
    private javax.swing.JCheckBox SaidaCanal3;
    private javax.swing.JCheckBox SaidaCanal4;
    private javax.swing.JCheckBox SaidaCanal5;
    private javax.swing.JCheckBox SaidaCanal6;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    protected javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JMenu logMenuItem;
    private javax.swing.JCheckBox nivelObs1ChBox;
    private javax.swing.JCheckBox nivelObs2ChBox;
    private javax.swing.JButton pararSinalBt;
    private javax.swing.JTable tabelaDetalhes;
    private javax.swing.JCheckBox tabelaKd;
    private javax.swing.JCheckBox tabelaKi;
    private javax.swing.JCheckBox tabelaKp;
    private javax.swing.JCheckBox tabelaMp;
    private javax.swing.JCheckBox tabelaSetPoint;
    private javax.swing.JCheckBox tabelaTd;
    private javax.swing.JCheckBox tabelaTi;
    private javax.swing.JCheckBox tabelaTpico;
    private javax.swing.JCheckBox tabelaTr100;
    private javax.swing.JCheckBox tabelaTr90;
    private javax.swing.JCheckBox tabelaTr95;
    private javax.swing.JCheckBox tabelaTs10;
    private javax.swing.JCheckBox tabelaTs2;
    private javax.swing.JCheckBox tabelaTs5;
    protected javax.swing.JProgressBar tanque1Progress;
    protected javax.swing.JProgressBar tanque2Progress;
    // End of variables declaration//GEN-END:variables
}
