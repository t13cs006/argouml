// $Id$
// Copyright (c) 1996-2009 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.diagram.static_structure.ui;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.apache.log4j.Logger;
import org.argouml.model.Model;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.diagram.ArgoDiagram;
import org.argouml.uml.diagram.DiagramSettings;
import org.tigris.gef.base.Selection;
import org.tigris.gef.presentation.Fig;

/**
 * Class to display graphics for a UML Interface in a diagram.
 * <p>
 * An Interface may show stereotypes and a compartment for
 * operations. Attributes are not supported in ArgoUML.
 */
public class FigInterface extends FigClassifierBox {

    private static final Logger LOG = Logger.getLogger(FigInterface.class);

    /**
     * Initialization common to multiple constructors.
     */
    private void initialize(Rectangle bounds) {
        // Put all the bits together, suppressing bounds calculations until
        // we're all done for efficiency.
        enableSizeChecking(false);
        setSuppressCalcBounds(true);

        getStereotypeFig().setKeyword("interface");
        getStereotypeFig().setVisible(true);
        /* The next line is needed so that we have the right dimension 
         * when drawing this Fig on the diagram by pressing down 
         * the mouse button, even before releasing the mouse button: */
        getNameFig().setTopMargin(
                getStereotypeFig().getMinimumSize().height);
        
        addFig(getBigPort());
        addFig(getNameFig());
        // stereotype fig covers the name fig:
        addFig(getStereotypeFig());
        addFig(getOperationsFig());
        addFig(getBorderFig());

        /* Set the drop location in the case of D&D: */
        if (bounds != null) {
            setLocation(bounds.x, bounds.y);
        }

        setSuppressCalcBounds(false);

        // Set the bounds of the figure to the total of the above 
        setBounds(getBounds());
        enableSizeChecking(true);
    }

    /**
     * Construct an Interface fig
     * 
     * @param owner owning UML element
     * @param bounds position and size
     * @param settings rendering settings
     */
    public FigInterface(Object owner, Rectangle bounds, 
            DiagramSettings settings) {
        super(owner, bounds, settings);
        initialize(bounds);
    }
    
    /*
     * @see org.tigris.gef.presentation.Fig#makeSelection()
     */
    @Override
    public Selection makeSelection() {
        return new SelectionInterface(this);
    }


    /**
     * Gets the minimum size permitted for an interface on the diagram.<p>
     *
     * Parts of this are hardcoded.<p>
     *
     * @return  the size of the minimum bounding box.
     */
    @Override
    public Dimension getMinimumSize() {
        // Use "aSize" to build up the minimum size. Start with the size of the
        // name compartment and build up.
        Dimension aSize = getNameFig().getMinimumSize();

        /* Only take into account the stereotype width, not the height, 
         * since the height is included in the name fig: */
        addChildWidth(aSize, getStereotypeFig());

        addChildDimensions(aSize, getOperationsFig());

        /* We want to maintain a minimum width for the 
         * interface fig. Also, add the border dimensions 
         * to the minimum space required for its contents: */
        aSize.width = Math.max(WIDTH, aSize.width);
        aSize.width += 2 * getLineWidth();
        aSize.height += 2 * getLineWidth();

        return aSize;
    }


    /*
     * @see org.tigris.gef.presentation.Fig#setEnclosingFig(org.tigris.gef.presentation.Fig)
     */
    @Override
    public void setEnclosingFig(Fig encloser) {
        Fig oldEncloser = getEnclosingFig();

        if (encloser == null
                || (encloser != null
                && !Model.getFacade().isAInstance(encloser.getOwner()))) {
            super.setEnclosingFig(encloser);
        }
        if (!(Model.getFacade().isAModelElement(getOwner()))) {
            return;
        }
        /* If this fig is not visible, do not adapt the UML model!
         * This is used for deleting. See issue 3042.
         */
        if  (!isVisible()) {
            return;
        }
        Object me = getOwner();
        Object m = null;

        try {
            // If moved into an Package
            if (encloser != null
                    && oldEncloser != encloser
                    && Model.getFacade().isAPackage(encloser.getOwner())) {
                Model.getCoreHelper().setNamespace(me, encloser.getOwner());
            }

            // If default Namespace is not already set
            if (Model.getFacade().getNamespace(me) == null
                    && (TargetManager.getInstance().getTarget()
                    instanceof ArgoDiagram)) {
                m =
                    ((ArgoDiagram) TargetManager.getInstance().getTarget())
                        .getNamespace();
                Model.getCoreHelper().setNamespace(me, m);
            }
        } catch (Exception e) {
            LOG.error("could not set package due to:" + e
                    + "' at " + encloser, e);
        }

        // The next if-clause is important for the Deployment-diagram
        // it detects if the enclosing fig is a component, in this case
        // the container will be set for the owning Interface
        if (encloser != null
                && (Model.getFacade().isAComponent(encloser.getOwner()))) {
            moveIntoComponent(encloser);
            super.setEnclosingFig(encloser);
        }
    }

    /**
     * USED BY PGML.tee.
     * @return the class name and bounds together with compartment
     * visibility.
     * TODO: Is this not duplicate with the parent?
     */
    @Override
    public String classNameAndBounds() {
        return super.classNameAndBounds()
                + "operationsVisible=" + isOperationsVisible();
    }

    /**
     * Sets the bounds, but the size will be at least the one returned by
     * {@link #getMinimumSize()}, unless checking of size is disabled.<p>
     *
     * If the required height is bigger, then the additional height is
     * equally distributed among all figs (i.e. compartments), such that the
     * accumulated height of all visible figs equals the demanded height.
     *
     * @param x  Desired X coordinate of upper left corner
     *
     * @param y  Desired Y coordinate of upper left corner
     *
     * @param w  Desired width of the FigInterface
     *
     * @param h  Desired height of the FigInterface
     */
    @Override
    protected void setStandardBounds(final int x, final int y, final int w,
            final int h) {
        /* Save our old boundaries (needed later), and get minimum size
         * info.*/ 
        Rectangle oldBounds = getBounds();

        /* The new size can not be smaller than the minimum. */
        Dimension minimumSize = getMinimumSize();
        int newW = Math.max(w, minimumSize.width);
        int newH = Math.max(h, minimumSize.height);

        int currentHeight = 0;

        if (getStereotypeFig().isVisible()) {
            int stereotypeHeight = getStereotypeFig().getMinimumSize().height;
            getNameFig().setTopMargin(stereotypeHeight);
            getStereotypeFig().setBounds(
                    x + getLineWidth(),
                    y + getLineWidth(),
                    newW - 2 * getLineWidth(),
                    stereotypeHeight);
        } else {
            getNameFig().setTopMargin(0);
        }
        
        /* Now the new nameFig height will include the stereotype height: */
        Dimension nameMin = getNameFig().getMinimumSize();
        int minNameHeight = Math.max(nameMin.height, NAME_FIG_HEIGHT);
        
        getNameFig().setBounds(
                x + getLineWidth(), 
                y + getLineWidth(), 
                newW - 2 * getLineWidth(), 
                minNameHeight);
        
        /* The new height can not be less than the name height: */
        /* TODO: Is this needed/correct? */
        newH = Math.max(minNameHeight, newH);
        
        currentHeight += minNameHeight;

        /* And the operations compartment takes the remainder 
         * of the requested height: */
        if (getOperationsFig().isVisible()) {
            int operationsHeight = newH - currentHeight - 2 * getLineWidth();
            /* If the requested height is smaller than the minimum required, ... */
            if ( operationsHeight < getOperationsFig().getMinimumSize().height) {
                /* ... then we use the minimum ... */
                operationsHeight = getOperationsFig().getMinimumSize().height;
                /* ... and make the Fig bigger: */
                newH += getOperationsFig().getMinimumSize().height - operationsHeight;
            }
            getOperationsFig().setBounds(
                    x + getLineWidth(),
                    y + currentHeight + getLineWidth(),
                    newW - 2 * getLineWidth(),
                    operationsHeight);
        }
        
        // set bounds of big box
        getBigPort().setBounds(x, y, newW, newH);
        getBorderFig().setBounds(x, y, newW, newH);

        // Now force calculation of the bounds of the figure, update the edges
        // and trigger anyone who's listening to see if the "bounds" property
        // has changed.

        calcBounds();
        updateEdges();
        firePropChange("bounds", oldBounds, getBounds());
    }

}