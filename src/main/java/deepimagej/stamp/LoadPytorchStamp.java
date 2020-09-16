/*
 * DeepImageJ
 * 
 * https://deepimagej.github.io/deepimagej/
 *
 * Conditions of use: You are free to use this software for research or educational purposes. 
 * In addition, we expect you to include adequate citations and acknowledgments whenever you 
 * present or publish results that are based on it.
 * 
 * Reference: DeepImageJ: A user-friendly plugin to run deep learning models in ImageJ
 * E. Gomez-de-Mariscal, C. Garcia-Lopez-de-Haro, L. Donati, M. Unser, A. Munoz-Barrutia, D. Sage. 
 * Submitted 2019.
 *
 * Bioengineering and Aerospace Engineering Department, Universidad Carlos III de Madrid, Spain
 * Biomedical Imaging Group, Ecole polytechnique federale de Lausanne (EPFL), Switzerland
 *
 * Corresponding authors: mamunozb@ing.uc3m.es, daniel.sage@epfl.ch
 *
 */

/*
 * Copyright 2019. Universidad Carlos III, Madrid, Spain and EPFL, Lausanne, Switzerland.
 * 
 * This file is part of DeepImageJ.
 * 
 * DeepImageJ is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * DeepImageJ is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DeepImageJ. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package deepimagej.stamp;
import java.awt.BorderLayout;
import java.awt.TextField;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.framework.SignatureDef;

import ai.djl.MalformedModelException;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import deepimagej.BuildDialog;
import deepimagej.Constants;
import deepimagej.Parameters;
import deepimagej.TensorFlowModel;
import deepimagej.components.HTMLPane;
import deepimagej.tools.DijTensor;
import deepimagej.tools.FileTools;
import deepimagej.tools.Log;
import ij.IJ;
import ij.gui.GenericDialog;

public class LoadPytorchStamp extends AbstractStamp implements Runnable {

	private String				name;
	private JTextField			inpNumber = new JTextField();
	private JTextField			outNumber = new JTextField();

	private HTMLPane			pnLoad;
	

	public LoadPytorchStamp(BuildDialog parent) {
		// TODO review messages
		super(parent);
		buildPanel();
	}

	@Override
	public void buildPanel() {
		pnLoad = new HTMLPane(Constants.width, 70);

		HTMLPane pnTag = new HTMLPane(Constants.width / 2, 70);
		pnTag.append("h2", "Number of inputs");
		pnTag.append("p", "Number of inputs to the Pytorch model");

		HTMLPane pnGraph = new HTMLPane(2 * Constants.width / 2, 70);
		pnGraph.append("h2", "Number of outputs");
		pnGraph.append("p", "Number of outputs of the Pytorch model.");

		JPanel pn = new JPanel();
		pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
		pn.add(pnTag.getPane());
		pn.add(inpNumber);
		inpNumber.setText("0");
		inpNumber.setEnabled(false);
		pn.add(pnGraph.getPane());
		pn.add(outNumber);
		outNumber.setText("0");
		outNumber.setEnabled(false);
		
		JPanel main = new JPanel(new BorderLayout());
		main.add(pnLoad.getPane(), BorderLayout.CENTER);
		main.add(pn, BorderLayout.SOUTH);
		panel.add(main);
	}

	@Override
	public void init() {
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	@Override
	public boolean finish() {
		Parameters params = parent.getDeepPlugin().params;
		params.totalInputList = new ArrayList<DijTensor>();
		params.totalOutputList = new ArrayList<DijTensor>();
		boolean inp = false;
		try {
			int nInp = Integer.parseInt(inpNumber.getText().trim());
			inp = true;
			int nOut = Integer.parseInt(outNumber.getText().trim());
			if (nOut < 1) {
				IJ.error("The number of outputs shoud be 1 or bigger");
				return false;
			} else if (nInp < 1) {
				IJ.error("The number of inputs shoud be 1 or bigger");
				return false;
			}
			for (int i = 0; i < nInp; i ++) {
				// TODO when possible add dimensions from model
				DijTensor inpT = new DijTensor("input" + i);
				params.totalInputList.add(inpT);
			}
			for (int i = 0; i < nOut; i ++) {
				// TODO when possible add dimensions from model
				DijTensor outT = new DijTensor("output" + i);
				params.totalOutputList.add(outT);
			}
			return true;
			
		} catch (Exception ex) {
			if (!inp) {
				IJ.error("Please introduce a valid integer for the number of inputs.");
			} else if (inp) {
				IJ.error("Please introduce a valid integer for the number of outputs.");
			}
			return false;
		}
	}

	public void run() {
		pnLoad.setCaretPosition(0);
		pnLoad.setText("");
		// TODO should we warn that DJL is loading?
		//pnLoad.append("p", "Loading available Tensorflow version.");
		
		Parameters params = parent.getDeepPlugin().params;
		// TODO refer to the DJL or Pytorch versions used if possible
		pnLoad.clear();
		pnLoad.append("h2", "Pytorch/Deep Java Library version");
		pnLoad.append("p", "Currently using XXXX framework");
		pnLoad.append("h2", "Model info");
		pnLoad.append("p", "Path: " + params.torchscriptPath);
		pnLoad.append("p", "Loading model...");
		
		// Load the model using DJL
		// TODO allow the use of translators and transforms
		URL url;
		// Block back button while loading
		parent.setEnabledBackNext(false);
		try {
			url = new File(new File(params.torchscriptPath).getParent()).toURI().toURL();
			
			String modelName = new File(params.torchscriptPath).getName();
			long startTime = System.nanoTime();
			modelName = modelName.substring(0, modelName.indexOf(".pt"));
			Criteria<NDList, NDList> criteria = Criteria.builder()
			        .setTypes(NDList.class, NDList.class)
			         // only search the model in local directory
			         // "ai.djl.localmodelzoo:{name of the model}"
			        .optModelUrls(url.toString()) // search models in specified path
			        //.optArtifactId("ai.djl.localmodelzoo:resnet_18") // defines which model to load
			        .optModelName(modelName)
			        .optProgress(new ProgressBar()).build();

			ZooModel<NDList, NDList> model = ModelZoo.loadModel(criteria);
			parent.getDeepPlugin().setTorchModel(model);
			String torchscriptSize = FileTools.getFolderSizeKb(params.torchscriptPath);
			long stopTime = System.nanoTime();
			// Convert nanoseconds into seconds
			String loadingTime = "" + ((stopTime - startTime) / (float) 1000000000);
			pnLoad.setCaretPosition(1);
			pnLoad.append("p", "Model size: " + torchscriptSize);
			pnLoad.append("p", "Loading time: " + loadingTime +  " s");
			
			parent.setEnabledBackNext(true);
			inpNumber.setEnabled(true);
			outNumber.setEnabled(true);
		} catch (MalformedURLException e) {
			pnLoad.append("p", "DeepImageJ could not load the model");
			pnLoad.append("p", "Check that the path provided to the model remains the same.");
			parent.setEnabledBack(true);
			e.printStackTrace();
		} catch (ModelNotFoundException e) {
			pnLoad.append("p", "DeepImageJ could not load the model");
			pnLoad.append("p", "No model was found in the path provided.");
			parent.setEnabledBack(true);
			e.printStackTrace();
		} catch (MalformedModelException e) {
			pnLoad.append("p", "DeepImageJ could not load the model");
			pnLoad.append("p", "The model provided is not a correct Torchscript model.");
			parent.setEnabledBack(true);
			e.printStackTrace();
		} catch (IOException e) {
			pnLoad.append("p", "DeepImageJ could not load the model");
			pnLoad.append("p", "Error whie accessing the model file.");
			parent.setEnabledBack(true);
			e.printStackTrace();
		}
	} 
}