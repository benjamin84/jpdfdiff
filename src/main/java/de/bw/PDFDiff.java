/*
 * Copyright (c) 2024. benjamin.wilger@gmail.com
 */

package de.bw;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.text.TextAction;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFDiff extends JFrame
{
   private final PDFViewerPane leftPdfViewer;
   private final PDFViewerPane rightPdfViewer;
   private JButton lastPageButton;
   private JButton nextPageButton;
   private JButton previousPageButton;
   private JButton firstPageButton;

   PDFDiff (PDFDocument leftDocument, PDFDocument rightDocument) throws HeadlessException
   {
      super ("PDF Compare");

      int maxPages = Math.max (leftDocument.getDocument ().getNumberOfPages (), rightDocument.getDocument ().getNumberOfPages ());
      int minPages = Math.min (leftDocument.getDocument ().getNumberOfPages (), rightDocument.getDocument ().getNumberOfPages ());

      JPanel mainPanel = new JPanel (new BorderLayout (2, 2));
      JSplitPane splitPane = createSplitter ();

      leftPdfViewer = new PDFViewerPane (leftDocument);
      rightPdfViewer = new PDFViewerPane (rightDocument);

      JPanel northContainer = new JPanel (new FlowLayout (FlowLayout.LEFT));
      buildSpinner (northContainer, minPages, maxPages);

      mainPanel.add (northContainer, BorderLayout.NORTH);
      setPreferredSize (new Dimension (1500, 1200));
      addWindowListener (new WindowAdapter ()
      {
         @Override
         public void windowClosed (WindowEvent e)
         {
            super.windowClosed (e);
            try
            {
               leftDocument.close ();
               rightDocument.close ();
            }
            catch (IOException ex)
            {
               throw new RuntimeException (ex);
            }
         }
      });

      KeyboardFocusManager.getCurrentKeyboardFocusManager ()
            .addKeyEventDispatcher (e -> {
               if (e.getKeyCode () == KeyEvent.VK_ESCAPE)
               {
                  PDFDiff.this.dispose ();
                  return true;
               }
               return false;
            });


      splitPane.setLeftComponent (leftPdfViewer);
      splitPane.setRightComponent (rightPdfViewer);
      splitPane.setEnabled (false);
      mainPanel.add (splitPane, BorderLayout.CENTER);

      add (mainPanel);
      pack ();
      splitPane.setDividerLocation (0.5);
      setLocationRelativeTo (null);
      setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);

      addComponentListener (new ComponentAdapter ()
      {
         @Override
         public void componentResized (ComponentEvent e)
         {
            super.componentResized (e);
            splitPane.setDividerLocation (0.5);
         }
      });
   }

   public static void main (String[] args)
   {
      File leftFile, rightFile = null;
      // No arguments or wrong count -> show file chooser
      if (args.length != 2)
      {
         leftFile = selectFile ("Select left file", null);
         // Handle user cancellation
         if (leftFile != null)
            rightFile = selectFile ("Select right file", leftFile.getParentFile ());
      }
      else
      {
         leftFile = new File (args[0]);
         rightFile = new File (args[1]);
      }

      if (leftFile == null || rightFile == null)
         return;

      PDDocument leftDoc = loadDocument (leftFile);
      if (leftDoc == null)
      {
         System.exit (1);
         return;
      }
      PDDocument rightDoc = loadDocument (rightFile);
      if (rightDoc == null)
      {
         System.exit (2);
         return;
      }
      PDFDocument leftDocument = new PDFDocument (leftFile, leftDoc, Side.LEFT);
      PDFDocument rightDocument = new PDFDocument (rightFile, rightDoc, Side.RIGHT);
      new PDFDiff (leftDocument, rightDocument).setVisible (true);
   }

   private static JSplitPane createSplitter ()
   {
      JSplitPane splitPane = new JSplitPane ();
      SplitPaneUI spui = splitPane.getUI ();
      if (spui instanceof BasicSplitPaneUI)
      {
         ((BasicSplitPaneUI) spui).getDivider ().addMouseListener (new MouseAdapter ()
         {
            @Override
            public void mouseClicked (MouseEvent arg0)
            {
               if (arg0.getClickCount () == 2)
               {
                  splitPane.setDividerLocation (0.5);
               }
            }
         });
      }
      return splitPane;
   }

   private static PDDocument loadDocument (File file2)
   {
      PDDocument doc2 = null;
      try
      {
         doc2 = Loader.loadPDF (file2);
      }
      catch (IOException e)
      {
         showError (file2, e);
         return null;
      }
      return doc2;
   }

   private static File selectFile (String title, File parentDir)
   {
      JFileChooser chooser = new JFileChooser ();
      chooser.setDialogTitle (title);
      if (parentDir != null) // Optionally navigate to the given directory
         chooser.setCurrentDirectory (parentDir);
      // Filter for pdf files
      chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
      chooser.setFileFilter (new FileNameExtensionFilter ("PDF files", "pdf"));

      chooser.showOpenDialog (null);

      return chooser.getSelectedFile ();
   }

   private static void showError (File file, IOException e)
   {
      JOptionPane.showMessageDialog (null,
            "Unable to open file " + file + "\n\nException: " + e.toString (),
            "Startup error", JOptionPane.ERROR_MESSAGE);
   }

   private void buildSpinner (JPanel northContainer, int minPages, int maxPages)
   {
      AbstractSpinnerModel spinnerModel;
      spinnerModel = new SpinnerNumberModel (1, 1,
            maxPages,
            1);
      spinnerModel.addChangeListener (e -> {
         int pageIdx = ((Number) spinnerModel.getValue ()).intValue () - 1;

         leftPdfViewer.setPage (pageIdx);
         rightPdfViewer.setPage (pageIdx);

         updateButtonStates (pageIdx, maxPages);
      });

      final JSpinner spinner;
      spinner = new JSpinner (spinnerModel);
      northContainer.add (new JLabel ("Page: "));
      firstPageButton = new JButton (new TextAction ("<<")
      {
         @Override
         public void actionPerformed (ActionEvent e)
         {
            spinnerModel.setValue (1);
         }
      });
      previousPageButton = new JButton (new TextAction ("<")
      {
         @Override
         public void actionPerformed (ActionEvent e)
         {
            Object previousValue = spinnerModel.getPreviousValue ();
            if (previousValue != null)
               spinnerModel.setValue (previousValue);
         }
      });
      nextPageButton = new JButton (new TextAction (">")
      {
         @Override
         public void actionPerformed (ActionEvent e)
         {
            Object nextValue = spinnerModel.getNextValue ();
            if (nextValue != null)
               spinnerModel.setValue (nextValue);
         }
      });
      lastPageButton = new JButton (new TextAction (">>")
      {
         @Override
         public void actionPerformed (ActionEvent e)
         {
            spinnerModel.setValue (maxPages);
         }
      });

      updateButtonStates (0, maxPages);

      northContainer.add (firstPageButton);
      northContainer.add (previousPageButton);
      northContainer.add (spinner);
      northContainer.add (nextPageButton);
      northContainer.add (lastPageButton);
   }

   private void updateButtonStates (int pageIdx, int maxPages)
   {
      firstPageButton.setEnabled (pageIdx > 0);
      previousPageButton.setEnabled (pageIdx > 0);
      nextPageButton.setEnabled (pageIdx < (maxPages - 1));
      lastPageButton.setEnabled (pageIdx < (maxPages - 1));
   }

}