// Copyright (c) 1995, 1996 Regents of the University of California.
// All rights reserved.
//
// This software was developed by the Arcadia project
// at the University of California, Irvine.
//
// Redistribution and use in source and binary forms are permitted
// provided that the above copyright notice and this paragraph are
// duplicated in all such forms and that any documentation,
// advertising materials, and other materials related to such
// distribution and use acknowledge that the software was developed
// by the University of California, Irvine.  The name of the
// University may not be used to endorse or promote products derived
// from this software without specific prior written permission.
// THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
// WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.

// File: ActionEditNode.java
// Classes: ActionEditNode
// Original Author: jrobbins@ics.uci.edu
// $Id$

package uci.gef;

import java.util.*;
import java.awt.*;
import java.io.*;

/** Action to edit a node.  For now this just asks the node to edit
 *  itself via editNode().  In the future their might be some support
 *  for undo or some behavior common to all node editing operations.
 *
 * @see NetNode#editNode */

public class ActionEditNode extends Action {

  private NetNode _nodeToEdit;

  public ActionEditNode() { _nodeToEdit = null; }

  public ActionEditNode(NetNode n) { _nodeToEdit = n; }

  public String name() { return "Edit Node"; }

  public void doIt(java.awt.Event e) {
    if (_nodeToEdit != null) {
      _nodeToEdit.editNode();
      return;
    }
    Editor ce = Globals.curEditor();
    if (ce == null) return;
    Vector selectedFigs = ce.selectedFigs();
    Enumeration figs = selectedFigs.elements();
    while (figs.hasMoreElements()) {
      Fig f = (Fig) figs.nextElement();
      if (f instanceof FigNode) {
        _nodeToEdit = (NetNode)((FigNode)f).getOwner();
	Globals.showStatus("Editing " + _nodeToEdit.toString());
        _nodeToEdit.editNode();
      }
    }
    _nodeToEdit = null;
  }

  public void undoIt() {
    System.out.println("Undo does not make sense for ActionEditNode");
  }

} /* end class ActionEditNode */
