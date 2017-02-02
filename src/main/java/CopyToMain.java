import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trantuanan on 2/1/17.
 */
public class CopyToMain {

    private JLabel lbFromFolder;
    private JPanel mainPanel;
    private JTextField txtFromFolder;
    private JTextField txtToFolder;
    private JList listFrom;
    private JList listTo;
    private JButton btnFromConfirm;
    private JButton btnToConfirm;
    private JTextField txtInfoTitle;
    private JPanel infoJPanel;
    private JTextField txtInfoStatus;
    private JList listChildFiles;
    private JButton copyButton;
    private JButton btnPreview;
    private JButton saveButton;
    private String[] formElements;
    private String[] toElements;

    private File selectedFolder;
    public CopyToMain() {
        init();
        btnFromConfirm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getListFrom();
            }

        });
        listFrom.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                listTo.clearSelection();
                String id  = (String)listFrom.getSelectedValue();
                getInfo(txtFromFolder.getText(),id);
            }
        });
        listFrom.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                String id  = (String)listFrom.getSelectedValue();
                getInfo(txtFromFolder.getText(),id);
            }
        });
        btnPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedFolder == null){
                    System.out.println("Selected folder null. Nothing thing to show");
                    return;
                }
                File[] flvFiles = selectedFolder.listFiles(UploadYouTube.flvFileName);
                if (flvFiles.length == 1){
                    File playFile = flvFiles[0];
                    try {
                        Desktop.getDesktop().open(playFile);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }else {
                    System.out.println("[Error] Flv file length = " + flvFiles.length);
                }
            }
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File[] flvFiles = selectedFolder.listFiles(UploadYouTube.endWithFlvFileName);
                if (flvFiles.length==1){
                    File targetFile = flvFiles[0];
                    try {
                        FileUtils.moveFile(targetFile,new File(targetFile.getParent()+"/"+txtInfoTitle.getText()));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }else {
                    System.out.println("[Error] flvFile length = " + flvFiles.length);
                }
                String id  = (String)listFrom.getSelectedValue();
                getInfo(txtFromFolder.getText(),id);


            }
        });
        btnToConfirm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getListTo();

            }
        });
        copyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedFolder==null){
                    return;
                }
                try {
                    FileUtils.copyDirectoryToDirectory(selectedFolder,new File(txtToFolder.getText()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                getListTo();

            }
        });
        listTo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listFrom.clearSelection();
                String id  = (String)listTo.getSelectedValue();
                getInfo(txtToFolder.getText(),id);


            }
        });
    }

    private void getListTo() {
        File parentFolder = new File(txtToFolder.getText());
        File[] file = parentFolder.listFiles();
        List<String> fileNames = getFileNames(file);
        listTo.setListData(fileNames.toArray());

    }

    private void init(){
        this.getListFrom();
        this.getListTo();
    }

    private void getInfo(String mainDirectory, final String id){
        File parentFolder = new File(mainDirectory);
        String title = "unknown";
        File[] file = parentFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.equals(id)){
                    return true;
                }
                return false;
            }
        });
        if (file.length==1){
            selectedFolder = file[0];
            File[] subFiles  = selectedFolder.listFiles();
            List<String> fileNames = getFileNames(subFiles);
            listChildFiles.setListData(fileNames.toArray());
            File[] flvFiles = selectedFolder.listFiles(UploadYouTube.endWithFlvFileName);
            if (flvFiles.length==1){
                title = flvFiles[0].getName();
            }
            File[] allFlvFiles = selectedFolder.listFiles(UploadYouTube.flvFileName);
            if (allFlvFiles.length>1){
                txtInfoStatus.setText("Streaming...");
            }else if (allFlvFiles.length == 1){
                File[] doneFiles = selectedFolder.listFiles(UploadYouTube.doneFileName);
                if (doneFiles.length==1){
                    txtInfoStatus.setText("DONE");
                }else {
                    txtInfoStatus.setText("Just init...");
                }
            }else {
                txtInfoStatus.setText("Error");
            }
            txtInfoTitle.setText(title);

        }else {
            System.out.println("Can't find target. id = " + id );
        }


    }

    private void getListFrom(){
        File parentFolder = new File(txtFromFolder.getText());
        File[] file = parentFolder.listFiles();
        List<String> fileNames = getFileNames(file);
        listFrom.setListData(fileNames.toArray());
    }
    private List<String> getFileNames(File[] files){
        List<String> sFilenames = new ArrayList<String>();
        if (files == null){
            return sFilenames;
        }else {
            for (File f : files) {
                sFilenames.add(f.getName());
            }
        }
        return sFilenames;
    }

    public static void main(String[] args){
        CopyToMain copyToMain = new CopyToMain();
        JFrame jFrame = new JFrame("Copy to main");
        jFrame.setContentPane(copyToMain.mainPanel);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setSize(800,800);
        jFrame.setResizable(true);
        jFrame.setVisible(true);

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
