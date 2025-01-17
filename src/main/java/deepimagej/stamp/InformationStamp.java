/*
 * DeepImageJ
 * 
 * https://deepimagej.github.io/deepimagej/
 * 
 * Reference: DeepImageJ: A user-friendly environment to run deep learning models in ImageJ
 * E. Gomez-de-Mariscal, C. Garcia-Lopez-de-Haro, W. Ouyang, L. Donati, M. Unser, E. Lundberg, A. Munoz-Barrutia, D. Sage. 
 * Submitted 2021.
 * Bioengineering and Aerospace Engineering Department, Universidad Carlos III de Madrid, Spain
 * Biomedical Imaging Group, Ecole polytechnique federale de Lausanne (EPFL), Switzerland
 * Science for Life Laboratory, School of Engineering Sciences in Chemistry, Biotechnology and Health, KTH - Royal Institute of Technology, Sweden
 * 
 * Authors: Carlos Garcia-Lopez-de-Haro and Estibaliz Gomez-de-Mariscal
 *
 */

/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2019-2021, DeepImageJ
 * All rights reserved.
 *	
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *	  this list of conditions and the following disclaimer in the documentation
 *	  and/or other materials provided with the distribution.
 *	
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package deepimagej.stamp;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import deepimagej.BuildDialog;
import deepimagej.Constants;
import deepimagej.Parameters;
import deepimagej.components.HTMLPane;
import ij.IJ;
import ij.gui.GenericDialog;

public class InformationStamp extends AbstractStamp implements ActionListener {

	public JTextField	txtName			= new JTextField("", 24);

	public JTextField	txtAuth		= new JTextField("", 24);
	public JTextField	txtTag		= new JTextField("", 24);

	public JTextField	txtDocumentation	= new JTextField("", 24);
	public JTextField	txtGitRepo	= new JTextField("", 24);
	public JTextField	txtLicense			= new JTextField("", 24);
	public JTextArea	txtDescription		= new JTextArea("", 3, 24);
	
	public JList<HashMap<String, String>> authList		= new JList<HashMap<String, String>>();
	public JList<String> tagList						= new JList<String>();
	public JList<HashMap<String, String>> citationList	= new JList<HashMap<String, String>>();
	
	private DefaultListModel<HashMap<String, String>> 	authModel;
	private DefaultListModel<String> 	tagModel;
	private DefaultListModel<HashMap<String, String>> 	citationModel;

	public JButton		authAddBtn		= new JButton("Add");
	public JButton		authRmvBtn		= new JButton("Remove");

	public JButton		tagAddBtn		= new JButton("Add");
	public JButton		tagRmvBtn		= new JButton("Remove");

	public JButton		citationAddBtn	= new JButton("Add");
	public JButton		citationRmvBtn	= new JButton("Remove");
	
	public ArrayList<HashMap<String, String>> introducedAuth = new ArrayList<HashMap<String, String>>();
	public ArrayList<String> introducedTag = new ArrayList<String>();
	public ArrayList<HashMap<String, String>> introducedCitation = new ArrayList<HashMap<String, String>>();
	
	// Key words for a class method to know whether to build
	// a citation panel or an authorship panel
	private String authTag = "auth";
	private String citeTag = "cite";
	
	// Parameter to keep track of the model being used
	public String		model			= ""; 

	public InformationStamp(BuildDialog parent) {
		super(parent);
		buildPanel();
	}

	@Override
	public void buildPanel() {
		HTMLPane pane = new HTMLPane(Constants.width, 80);
		pane.setBorder(BorderFactory.createEtchedBorder());
		pane.append("h2", "General Information");
		pane.append("p", "This information will be stored in the config.yaml");
		pane.append("p", "Add the reference to properly cite the pretrained model.");
		txtDescription.setBorder(BorderFactory.createLineBorder(Color.gray));

		JFrame pnFr = new JFrame();
		Container pn = pnFr.getContentPane();
		pn.setLayout(new GridBagLayout()); 

		GridBagConstraints  labelC = new GridBagConstraints();
		labelC.gridwidth = 4;
		labelC.gridheight = 1;
		labelC.gridx = 0;
		labelC.gridy = 0;
		labelC.ipadx = 5;
		labelC.weightx = 0.2;

		GridBagConstraints  infoC = new GridBagConstraints();
		infoC.gridwidth = 20;
		infoC.gridheight = 1;
		infoC.gridx = 4;
		infoC.gridy = 0;
		infoC.ipadx = 5;
		infoC.weightx = 0.8;
		infoC.anchor = GridBagConstraints.CENTER;
	    infoC.fill = GridBagConstraints.BOTH;
	    infoC.insets = new Insets(10, 0, 10, 10); 

		// MOdel name field
		pn.add(new JLabel("Full name"), labelC);
		pn.add(txtName, infoC);
		
		// Authorship field
		labelC.gridy = 1;
		labelC.ipadx = 0;
		labelC.ipady = 0;
		infoC.gridy = 1;
	    infoC.insets = new Insets(0, 0, 0, 0);
		infoC.ipady = 50; 
		infoC.ipadx = 50; 
		JFrame authorsFr = createAddRemoveCitation(authAddBtn, authRmvBtn, authTag);
		pn.add(new JLabel("Authors of the bundled model"), labelC);
		authorsFr.getContentPane().setSize(8, 20);
		pn.add((JComponent) authorsFr.getContentPane(), infoC);
		
		// Citation field
		labelC.gridy = 2;
		labelC.ipadx = 0;
		labelC.ipady = 0;
		infoC.gridy = 2;
	    infoC.insets = new Insets(0, 0, 0, 0);
		infoC.ipady = 50; 
		infoC.ipadx = 50; 
		JFrame citationsFr = createAddRemoveCitation(citationAddBtn, citationRmvBtn, citeTag);
		pn.add(new JLabel("Citations"), labelC);
		citationsFr.getContentPane().setSize(8, 20);
		pn.add((JComponent) citationsFr.getContentPane(), infoC);
		
		// MOdel description field
		labelC.gridy = 4;
		labelC.gridheight = 3;
		labelC.ipadx = 50;
		labelC.ipady = 50;
		
		infoC.gridy = 4;
		infoC.gridheight = 3;

		infoC.ipady = 80; 
		infoC.ipady = 80; 
		infoC.ipadx = 0; 
	    infoC.insets = new Insets(0, 0, 0, 0);
		
		pn.add(new JLabel("<html>Description of the         model</html>"), labelC);
		txtDescription.setLineWrap(true);
		txtDescription.setWrapStyleWord(true);
		JScrollPane txtScroller = new JScrollPane(txtDescription);
		txtScroller.setPreferredSize(new Dimension(txtDescription.getPreferredSize().width, txtDescription.getPreferredSize().height + 50));

		pn.add(txtScroller, infoC);
		
		// Docs field
		labelC.gridy = 7;
		labelC.gridheight = 1;
		labelC.ipadx = 0;
		labelC.ipady = 0;
		infoC.gridy = 7;
		infoC.gridheight = 1;
	    infoC.insets = new Insets(10, 0, 10, 10);

		infoC.ipady = 0; 
		infoC.ipadx = 0;
		pn.add(new JLabel("Link to documentation"), labelC);
		pn.add(txtDocumentation, infoC);
		
		// GIT repo link field
		labelC.gridy = 8;
		labelC.gridheight = 1;
		labelC.ipadx = 0;
		labelC.ipady = 0;
		infoC.gridy = 8;
		infoC.gridheight = 1;
	    infoC.insets = new Insets(10, 0, 10, 10);

		infoC.ipady = 0; 
		infoC.ipadx = 0;
		pn.add(new JLabel("Link to Github repo"), labelC);
		pn.add(txtGitRepo, infoC);
		
		// Next field
		labelC.gridy = 9;
		infoC.gridy = 9;
		pn.add(new JLabel("Type of license"), labelC);
		pn.add(txtLicense, infoC);
		
		// TAgs field
		JFrame tagsFr = createAddRemoveFrame(txtTag, tagAddBtn, "tag", tagRmvBtn);

		labelC.gridy = 10;
		labelC.ipadx = 60;
		labelC.ipady = 60;
		infoC.gridy = 10;
		pn.add(new JLabel("<html>Tags to describe the model in the Bioimage Model Zoo</html>"), labelC);
		pn.add((JComponent) tagsFr.getContentPane(), infoC);
		
		JPanel p = new JPanel(new BorderLayout());
		
		JScrollPane scroll = new JScrollPane();
		pn.setPreferredSize(new Dimension(pn.getWidth() + 400, pn.getHeight() + 700));
        scroll.setPreferredSize(new Dimension(pn.getWidth() + 300, pn.getHeight() + 400));
        scroll.setViewportView(pn);
		
		p.add(pane, BorderLayout.NORTH);
		p.add(scroll, BorderLayout.CENTER);
		panel.add(p);
		

		// Add the tad 'deepImageJ' to the tags field. This tag
		// is not removable
		tagModel = new DefaultListModel<String>();
		tagModel.addElement("deepimagej");
		tagList.setModel(tagModel);
		introducedTag.add("deepimagej");
		
		authAddBtn.addActionListener(this);
		authRmvBtn.addActionListener(this);
		
		citationAddBtn.addActionListener(this);
		citationRmvBtn.addActionListener(this);
		
		tagAddBtn.addActionListener(this);
		tagRmvBtn.addActionListener(this);
	}
	
	@Override
	public void init() {
		File file = new File(parent.getDeepPlugin().params.path2Model);
		if (!model.equals(parent.getDeepPlugin().params.path2Model)) {
			txtName.setText(file.getName());
			model = parent.getDeepPlugin().params.path2Model;

			introducedAuth = new ArrayList<HashMap<String, String>>();
			authModel = new DefaultListModel<HashMap<String, String>>();
			authList.setModel(authModel);

			introducedCitation = new ArrayList<HashMap<String, String>>();
			citationModel = new DefaultListModel<HashMap<String, String>>();
			citationList.setModel(citationModel);
			// Add the tag 'deepImageJ' to the tags field. This tag
			// is not removable
			introducedTag = new ArrayList<String>();
			tagModel = new DefaultListModel<String>();
			tagModel.addElement("deepimagej");
			tagList.setModel(tagModel);
			introducedTag.add("deepimagej");
			
			// Reset all the fields
			txtAuth.setText("");
			txtTag.setText("");
			txtDocumentation.setText("");
			txtGitRepo.setText("");
			txtLicense.setText("");
			txtDescription.setText("");
		}
	}
	
	@Override
	public boolean finish() {
		if (txtName.getText().trim().equals("")) {
			IJ.error("The name is a mandatory field");
			return false;
		}
		Parameters params = parent.getDeepPlugin().params;
		params.name = txtName.getText().trim();

		// TODO check if we need to cover here
		params.documentation = txtDocumentation.getText().trim();
		params.git_repo = txtGitRepo.getText().trim();
		params.license = txtLicense.getText().trim();
		params.description = txtDescription.getText().trim();
		
		params.name = params.name.equals("") ? null : coverForbiddenSymbols(params.name);
		params.author = null;
		if (introducedAuth.size() > 0)
			params.author = introducedAuth;
		params.cite = introducedCitation;
		
		params.documentation = params.documentation.equals("") ? null : params.documentation;
		params.git_repo = params.git_repo.equals("") ? null : params.git_repo;
		params.license = params.license.equals("") ? null : coverForbiddenSymbols(params.license);
		params.description = params.description.equals("") ? null : coverForbiddenSymbols(params.description);
		params.infoTags = introducedTag;
		
		
		return true;
	}
	
	// TODO find more forbidden characters
	public static String coverForbiddenSymbols(String txt) {
		String[] forbidenCharacters = {":", "{", "}", "[", "]", ">", "=", "!",
									",", "&", "*", "#", "?", "|", "-", "<",
									"¡", "¿", "%", "@", "Ñ", "ñ"};
		for (String forbidenChar : forbidenCharacters) {
			if (txt.contains(forbidenChar)) {
				txt = "\'" + txt +  "\'";
				break;
			}
		}
		return txt;
	}
	
	public void addAuthor() {
		GenericDialog dlg = new GenericDialog("Add author information");
		dlg.addStringField("Name", "", 70);
		dlg.addStringField("Affiliation", "", 70);
		dlg.addStringField("Orcid", "", 70);
		dlg.showDialog();
		if (dlg.wasCanceled()) {
			return;
		}
		Vector<TextField> strField = dlg.getStringFields();
		TextField nameField = (TextField) strField.get(0);
		TextField affField = (TextField) strField.get(1);
		TextField orcidField = (TextField) strField.get(2);
		HashMap<String, String> specs = new HashMap<String, String>();
		specs.put("name", coverForbiddenSymbols(nameField.getText().trim()));
		if (specs.get("name").contentEquals(""))
			specs.put("name", "n/a");
		specs.put("affiliation", coverForbiddenSymbols(affField.getText().trim()));
		if (specs.get("affiliation").contentEquals(""))
			specs.put("affiliation", null);
		specs.put("orcid", coverForbiddenSymbols(orcidField.getText().trim()));
		if (specs.get("orcid").contentEquals(""))
			specs.put("orcid", null);

		introducedAuth.add(specs);

		authModel = new DefaultListModel<HashMap<String, String>>();
		
		// Add the elements to the list

		for (HashMap<String, String> name : introducedAuth){
			authModel.addElement(name);
		}
		authList.setModel(authModel);
		authList.setCellRenderer(new MyListCellRenderer(authTag));
	}
	
	public void removeAuthor() {
		// Get the author selected
		int citation = authList.getSelectedIndex();
		if (citation == -1) {
			IJ.error("No citation selected");
			return;
		}
		introducedAuth.remove(citation);

		authModel = new DefaultListModel<HashMap<String, String>>();
		
		// Add the elements to the list

		for (HashMap<String, String> name : introducedAuth){
			authModel.addElement(name);
		}
		authList.setModel(authModel);
		authList.setCellRenderer(new MyListCellRenderer(authTag));
	}
	
	public void addCite() {
		GenericDialog dlg = new GenericDialog("Add reference and its doi");
		dlg.addStringField("Reference", "", 70);
		dlg.addStringField("Doi", "http://", 70);
		dlg.showDialog();
		if (dlg.wasCanceled()) {
			return;
		}
		Vector<TextField> strField = dlg.getStringFields();
		TextField refField = (TextField) strField.get(0);
		TextField doiField = (TextField) strField.get(1);
		HashMap<String, String> refAndDoi = new HashMap<String, String>();
		String txt = coverForbiddenSymbols(refField.getText().trim());
		refAndDoi.put("text", txt);
		refAndDoi.put("doi", doiField.getText().trim());
        /* Try creating a valid URL */
		boolean url = false;
        try { 
            new URL(refAndDoi.get("doi")).toURI(); 
            url =  true; 
        } 
          
        // If there was an Exception 
        // while creating URL object 
        catch (Exception e) { 
            url =  false; 
        } 
		if (!url && !refAndDoi.get("doi").equals("")) {
			IJ.error("You need to introduce a valid URL in the doi field or leave it empty.");
			addCite();
			return;
		} 
		introducedCitation.add(refAndDoi);

		citationModel = new DefaultListModel<HashMap<String, String>>();
		
		// Add the elements to the list

		for (HashMap<String, String> name : introducedCitation){
			citationModel.addElement(name);
		}
		citationList.setModel(citationModel);
		citationList.setCellRenderer(new MyListCellRenderer(citeTag));
	}
	
	public void removeCite() {
		// Get the author selected
		int citation = citationList.getSelectedIndex();
		if (citation == -1) {
			IJ.error("No citation selected");
			return;
		}
		introducedCitation.remove(citation);

		citationModel = new DefaultListModel<HashMap<String, String>>();
		
		// Add the elements to the list

		for (HashMap<String, String> name : introducedCitation){
			citationModel.addElement(name);
		}
		citationList.setModel(citationModel);
		citationList.setCellRenderer(new MyListCellRenderer(citeTag));
	}
	
	public void addTag() {
		// Get the author introduced
		String tag = coverForbiddenSymbols(txtTag.getText().trim());
		if (tag.equals("")) {
			IJ.error("Introduce a name");
			return;
		}
		introducedTag.add(tag);

		tagModel = new DefaultListModel<String>();
		
		// Add the elements to the list

		for (String name : introducedTag){
			tagModel.addElement(name);
		}
		tagList.setModel(tagModel);
		txtTag.setText("");
	}
	public void removeTag() {
		// Get the author selected
		int tag = tagList.getSelectedIndex();
		if (tag == -1) {
			IJ.error("No tag selected");
			return;
		} else if (tag == 0) {
			IJ.error("Cannot remove 'deepimagej' tag");
			return;
		}
		introducedTag.remove(tag);

		tagModel = new DefaultListModel<String>();
		
		// Add the elements to the list

		for (String name : introducedTag){
			tagModel.addElement(name);
		}
		tagList.setModel(tagModel);
	}
	
	/*
	 * Method that creates the Gui component that allows adding and removing tags
	 */
	public JFrame createAddRemoveFrame(JTextField txt, JButton add, String option, JButton rmv) {
		// Create the panel to add authors
		JFrame authorsFr = new JFrame();
		authorsFr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		Container authorsPn = authorsFr.getContentPane();
		authorsPn.setLayout(new GridBagLayout()); 

	    // creates a constraints object 
	    GridBagConstraints c = new GridBagConstraints(); 
	    c.fill = GridBagConstraints.BOTH;
	    c.ipady = 5; 
	    c.ipadx = 20; 
	    c.weightx = 1;
	    c.gridx = 0;
	    c.gridy = 0;
	    c.gridwidth = 7;
	    authorsPn.add(txt, c);

	    c.ipady = 0; 
	    c.ipadx = 0; 
	    c.weightx = 0.2;
	    c.gridx = 7;
	    c.gridy = 0;
	    c.anchor = GridBagConstraints.CENTER;
	    c.fill = GridBagConstraints.NONE;
	    authorsPn.add(add, c);

	    c.ipady = 40; 
	    c.ipadx = 20; 
	    c.weightx = 1;
	    c.weighty = 1;
	    c.gridwidth = 7;
	    c.anchor = GridBagConstraints.CENTER;
	    c.fill = GridBagConstraints.BOTH;
	    c.gridx = 0;
	    c.gridy = 1;
	    if (option.contains("auth")) {
			//authModel = new DefaultListModel<String>();
			//authModel.addElement("");
			//authList = new JList<String>(authModel);
			authList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			authList.setLayoutOrientation(JList.VERTICAL);
			authList.setVisibleRowCount(2);
			JScrollPane listScroller = new JScrollPane(authList);
			listScroller.setPreferredSize(new Dimension(Constants.width, panel.getPreferredSize().height));
		    authorsPn.add(listScroller, c);
	    } else if(option.contains("tag")) {
			tagModel = new DefaultListModel<String>();
			tagModel.addElement("");
			tagList = new JList<String>(tagModel);
			tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tagList.setLayoutOrientation(JList.VERTICAL);
			tagList.setVisibleRowCount(2);
			JScrollPane listScroller = new JScrollPane(tagList);
			listScroller.setPreferredSize(new Dimension(Constants.width, panel.getPreferredSize().height));
		    authorsPn.add(listScroller, c);
	    }

	    c.ipady = 0; 
	    c.ipadx = 0; 
	    c.gridx = 7;
	    c.gridy = 1;
	    c.gridheight =1; 
	    c.anchor = GridBagConstraints.CENTER;
	    c.fill = GridBagConstraints.NONE;
	    c.weightx = 0.2;
	    Dimension btnDims = authAddBtn.getPreferredSize();
	    rmv.setPreferredSize(btnDims);
	    authorsPn.add(rmv, c);
	    authorsFr.pack();
	    return authorsFr;
	}
	
	/*
	 * Method that creates the Gui component that allows adding and removing citations
	 */
	public JFrame createAddRemoveCitation(JButton add, JButton rmv, String option) {
		// Create the panel to add authors
		JFrame authorsFr = new JFrame();
		authorsFr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		Container authorsPn = authorsFr.getContentPane();
		authorsPn.setLayout(new GridBagLayout()); 

	    // creates a constraints object 
	    GridBagConstraints c = new GridBagConstraints(); 
	    c.fill = GridBagConstraints.BOTH;
	    c.ipady = 60; 
	    c.ipadx = 20; 
	    c.weightx = 1;
	    c.weighty = 1;
	    c.gridwidth = 7;
	    c.anchor = GridBagConstraints.CENTER;
	    c.fill = GridBagConstraints.BOTH;
	    c.gridx = 0;
	    c.gridy = 0;
	    c.gridheight =2; 

	    if (option.contains(authTag)) {
	    	authModel = new DefaultListModel<HashMap<String, String>>();
	    	authList = new JList<HashMap<String, String>>(authModel);
	    	authList.setCellRenderer(new MyListCellRenderer(authTag));
	    	authList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    	authList.setLayoutOrientation(JList.VERTICAL);
	    	authList.setVisibleRowCount(4);
			JScrollPane listScroller = new JScrollPane(authList);
			listScroller.setPreferredSize(new Dimension(Constants.width, panel.getPreferredSize().height));
		    authorsPn.add(listScroller, c);
	    } else if(option.contains(citeTag)) {
	    	citationModel = new DefaultListModel<HashMap<String, String>>();
			citationList = new JList<HashMap<String, String>>(citationModel);
			citationList.setCellRenderer(new MyListCellRenderer(citeTag));
			citationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			citationList.setLayoutOrientation(JList.VERTICAL);
			citationList.setVisibleRowCount(4);
			JScrollPane listScroller = new JScrollPane(citationList);
			listScroller.setPreferredSize(new Dimension(Constants.width, panel.getPreferredSize().height));
		    authorsPn.add(listScroller, c);
	    }

	    c.ipady = 0; 
	    c.ipadx = 0; 
	    c.weightx = 0.2;
	    c.gridx = 7;
	    c.gridy = 0;
	    c.gridheight =1; 
	    c.anchor = GridBagConstraints.CENTER;
	    c.fill = GridBagConstraints.NONE;
	    authorsPn.add(add, c);


	    c.ipady = 0; 
	    c.ipadx = 0; 
	    c.gridx = 7;
	    c.gridy = 1;
	    c.gridheight =1; 
	    c.anchor = GridBagConstraints.CENTER;
	    c.fill = GridBagConstraints.NONE;
	    c.weightx = 0.2;
	    Dimension btnDims = authAddBtn.getPreferredSize();
	    rmv.setPreferredSize(btnDims);
	    authorsPn.add(rmv, c);
	    authorsFr.pack();
	    return authorsFr;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == tagAddBtn) {
			addTag();
		} 
		if (e.getSource() == tagRmvBtn) {
			removeTag();
		} 
		if (e.getSource() == authAddBtn) {
			addAuthor();
		} 
		if (e.getSource() == authRmvBtn) {
			removeAuthor();
		} 
		if (e.getSource() == citationAddBtn) {
			addCite();
		} 
		if (e.getSource() == citationRmvBtn) {
			removeCite();
		} 
	}

    private class MyListCellRenderer extends DefaultListCellRenderer {
    	
    	private String tag;
    	
    	public MyListCellRenderer(String tag) {
    		this.tag = tag;
    	}

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            HashMap<String, String> label = (HashMap<String, String>) value;
            if (tag.toLowerCase().contentEquals(citeTag)) {
	            String text = label.get("text");
	            String doi = label.get("doi");
	            if (label.keySet().size() > 0) {
		            String labelText = "<html>- " + text + "<br/>" + "  " + doi;
		            setText(labelText);
	            }
            } else if (tag.toLowerCase().contentEquals(authTag)) {
	            String name = label.get("name");
	            String aff = label.get("affiliation");
	            String orcid = label.get("orcid");
	            if (label.keySet().size() > 0) {
		            String labelText = "<html>- " + name + "<br/>" + "  " + aff + "<br/>" + "  " + orcid;
		            setText(labelText);
	            }
            }
            return this;
        }

    }
}
