/*
 * Copyright (c) 2024. benjamin.wilger@gmail.com
 */

package de.bw;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;

class PDFViewerPane extends JPanel
{
   private static final double PADDING = 5.0;
   private final PDFDocument document;
   private final TitlePane titlePane;
   private final RendererPane rendererPane;
   private final StatsPane statsPane;
   private int page;

   public PDFViewerPane (PDFDocument document)
   {
      this.document = document;
      rendererPane = new RendererPane ();
      statsPane = new StatsPane ();
      titlePane = new TitlePane ();

      setLayout (new BorderLayout ());
      add (titlePane, BorderLayout.NORTH);
      add (rendererPane, BorderLayout.CENTER);
      add (statsPane, BorderLayout.SOUTH);

      titlePane.updateTitle ();
      statsPane.updateStats ();
   }

   public void setPage (int page)
   {
      this.page = page;
      SwingUtilities.invokeLater (rendererPane::repaint);

      statsPane.updateStats ();
   }

   private class RendererPane extends JPanel
   {
      @Override
      public void paint (Graphics g)
      {
         super.paint (g);

         Graphics2D g2d = (Graphics2D) g;
         if (page < document.getDocument ().getNumberOfPages ())
         {
            PDFRenderer pdfRenderer = new PDFRenderer (document.getDocument ());
            try
            {
               PDRectangle cropBox = document.getDocument ().getPage (page).getCropBox ();
               float scale = calculateScale (cropBox, g2d.getClipBounds ());
               g2d.translate (
                     (g2d.getClipBounds ().getWidth () - cropBox.getWidth () * scale) / 2,
                     (g2d.getClipBounds ().getHeight () - cropBox.getHeight () * scale) / 2);
               pdfRenderer.renderPageToGraphics (page, (Graphics2D) g, scale, scale, RenderDestination.VIEW);
            }
            catch (IOException e)
            {
               throw new RuntimeException (e);
            }
         }
      }

      private float calculateScale (PDRectangle cropBox, Rectangle clipBounds)
      {
         double panelW = clipBounds.getWidth () - 2 * PADDING;
         double panelH = clipBounds.getHeight () - 2 * PADDING;

         double pdfSizeW = cropBox.getWidth ();
         double pdfSizeH = cropBox.getHeight ();

         return (float) Math.min (panelW / pdfSizeW, panelH / pdfSizeH);
      }
   }

   private class TitlePane extends JPanel
   {
      private final JLabel label = new JLabel ();

      public TitlePane ()
      {
         setLayout (new BoxLayout (this, BoxLayout.X_AXIS));
         add (label);
      }

      public void updateTitle ()
      {
         label.setText (document.getFile ().toString ());
      }
   }

   private class StatsPane extends JPanel
   {
      private final JLabel firstRow = new JLabel ();
      private final JLabel secondRow = new JLabel ();

      public StatsPane ()
      {
         setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
         add (firstRow);
         add (secondRow);
      }

      public void updateStats ()
      {
         PDDocument pdDoc = document.getDocument ();

         {
            StringBuilder builder = new StringBuilder ();
            if (page < pdDoc.getNumberOfPages ())
            {
               PDPage pdPage = pdDoc.getPage (page);
               builder.append ("Page: ").append (
                     page + 1).append (" / ").append (pdDoc.getPages ().getCount ()).append (", ");
               builder.append ("Dimensions: ").append (pdPage.getCropBox ());
            }
            else
            {
               builder.append ("Page: ").append ("N/A / ").append (pdDoc.getPages ().getCount ()).append (", ");
               builder.append ("Dimensions: N/A");
            }

            firstRow.setText (builder.toString ());
         }

         {
            StringBuilder builder = new StringBuilder ();
            if (pdDoc.getDocumentInformation () != null)
            {

               builder.append ("\n");
               builder.append ("Info: ");
               boolean first = true;
               for (String metadataKey : pdDoc.getDocumentInformation ().getMetadataKeys ())
               {
                  if (first)
                  {
                     first = false;
                  }
                  else
                  {
                     builder.append (", ");
                  }
                  builder.append (metadataKey).append ("=").append (pdDoc.getDocumentInformation ().getCOSObject ().getString (metadataKey));
               }
            }
            secondRow.setText (builder.toString ());
         }
      }
   }
}
