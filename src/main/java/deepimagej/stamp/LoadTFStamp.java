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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.framework.SignatureDef;

import deepimagej.BuildDialog;
import deepimagej.Constants;
import deepimagej.Parameters;
import deepimagej.DeepLearningModel;
import deepimagej.components.HTMLPane;
import deepimagej.tools.DijTensor;
import deepimagej.tools.FileTools;
import deepimagej.tools.Log;
import deepimagej.tools.StartTensorflowService;
import deepimagej.tools.SystemUsage;
import ij.IJ;

public class LoadTFStamp extends AbstractStamp implements Runnable {

	private ArrayList<String>	tags;
	private JComboBox<String>	cmbTags			= new JComboBox<String>();
	private JComboBox<String>	cmbGraphs		= new JComboBox<String>();
	//private ArrayList<String[]>	architecture	= new ArrayList<String[]>();
	private String				name;

	private HTMLPane			pnLoad;
	

	public LoadTFStamp(BuildDialog parent) {
		super(parent);
		tags = new ArrayList<String>();
		tags.add("Serve");
		buildPanel();
	}

	@Override
	public void buildPanel() {
		pnLoad = new HTMLPane(Constants.width, 70);

		HTMLPane pnTag = new HTMLPane(Constants.width / 2, 70);
		pnTag.append("h2", "Model Tag");
		pnTag.append("p", "Tag used to save the TensorFlow SavedModel. If the plugin cannot automatically find it, you will need to edit it.");

		HTMLPane pnGraph = new HTMLPane(2 * Constants.width / 2, 70);
		pnGraph.append("h2", "SignatureDef");
		pnGraph.append("p", "SignatureDef used to call the wanted model graph. There might be more than one in the same model folder.");

		JPanel pn = new JPanel();
		pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
		pn.add(pnTag.getPane());
		pn.add(cmbTags);
		pn.add(pnGraph.getPane());
		pn.add(cmbGraphs);
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
		if (params.tag == null) {
			Log log = new Log();
			String tag = (String)cmbTags.getSelectedItem();
			try {	
				double time = System.nanoTime();
				SavedModelBundle model = DeepLearningModel.loadTf(params.path2Model, tag, log);
				time = System.nanoTime() - time;
				addLoadInfo(params, time);
				parent.getDeepPlugin().setTfModel(model);
				params.tag = tag;
				cmbTags.setEditable(false);
				parent.getDeepPlugin().setTfModel(model);
				params.graphSet = DeepLearningModel.metaGraphsSet(model);
				if (params.graphSet.size() > 0) {
					Set<String> tfGraphSet = DeepLearningModel.returnTfSig(params.graphSet);
					cmbGraphs.removeAllItems();
					for (int i = 0; i < params.graphSet.size(); i++) {
						cmbGraphs.addItem((String) tfGraphSet.toArray()[i]);
						cmbGraphs.setEditable(false);
					}
				}
				
			}
			catch (Exception e) {
				IJ.error("Incorrect ModelTag");
				params.tag = null;
				cmbTags.removeAllItems();
				cmbTags.setEditable(true);
			}
			return false;
		} else {
			// TODO put it inside run
			SavedModelBundle model = parent.getDeepPlugin().getTfModel();
			params.graph = DeepLearningModel.returnStringSig((String)cmbGraphs.getSelectedItem());
			SignatureDef sig = DeepLearningModel.getSignatureFromGraph(model, params.graph);
			params.totalInputList = new ArrayList<>();
			params.totalOutputList = new ArrayList<>();
			String[] inputs = DeepLearningModel.returnTfInputs(sig);
			String[] outputs = DeepLearningModel.returnTfOutputs(sig);
			Arrays.sort(inputs);
			Arrays.sort(outputs);
			pnLoad.append("p", "Number of outputs: " + outputs.length);
			boolean valid = true;
			try {
				for (int i = 0; i < inputs.length; i ++) {
					DijTensor inp = new DijTensor(inputs[i]);
					inp.setInDimensions(DeepLearningModel.modelTfEntryDimensions(sig, inputs[i]));
					params.totalInputList.add(inp);
				}
				for (int i = 0; i < outputs.length; i ++) {
					DijTensor out = new DijTensor(outputs[i]);
					out.setInDimensions(DeepLearningModel.modelTfExitDimensions(sig, outputs[i]));
					params.totalOutputList.add(out);
				}
			}
			catch (Exception ex) {
				pnLoad.append("p", "Dimension: ERROR");
				valid  = false;
				parent.setEnabledBackNext(valid);
				return false;
			}
			parent.setEnabledBackNext(valid);
			return true;
		}
	}

	// TODO separate in methods
	public void run() {
		parent.setEnabledBack(false);
		parent.setEnabledNext(false);
		pnLoad.setCaretPosition(0);
		pnLoad.setText("");
		pnLoad.append("p", "Loading available Tensorflow version.");
		String loadInfo = "ImageJ";
		boolean isFiji = SystemUsage.checkFiji();
		if (isFiji)
			loadInfo = StartTensorflowService.loadTfLibrary();
		
		// If loadlLibrary() returns 'ImageJ', the plugin is running
		// on an ImageJ1 instance
		parent.setFiji(!loadInfo.contains("ImageJ"));
		pnLoad.setCaretPosition(0);
		pnLoad.setText("");
		if (loadInfo.equals("")) {
			pnLoad.append("p", "Unable to find any Tensorflow distribution.");
			pnLoad.append("p", "Please, install a valid Tensorflow version.");
			parent.setEnabledBack(true);
			return;
		}

		Parameters params = parent.getDeepPlugin().params;
		cmbTags.removeAllItems();
		cmbGraphs.removeAllItems();
		String tfVersion = DeepLearningModel.getTFVersion(parent.getFiji());
		pnLoad.clear();
		pnLoad.append("h2", "Tensorflow version");
		if (loadInfo.toLowerCase().contains("gpu"))
			tfVersion += "_GPU";
		pnLoad.append("p", "Currently using Tensorflow " + tfVersion);
		if (parent.getFiji()) {
			pnLoad.append("p", loadInfo);
		} else {
			pnLoad.append("p", "To change the Tensorflow version, download the corresponding\n"
							 + "libtensorflow and libtensorflow_jni jars and copy them into\n"
							 + "the plugins folder.");
		}
		// Run the nvidia-smi to see if it is possible to locate a GPU
		String cudaVersion = "";
		if (tfVersion.contains("GPU"))
			cudaVersion = SystemUsage.getCUDAEnvVariables();
		else
			parent.setGPUTf("CPU");
		// If a CUDA distribution was found, cudaVersion will be equal
		// to the CUDA version. If not it can be either 'noCuda', if CUDA 
		// is not installed, or if there is a CUDA_PATH in the environment variables
		// but the needed variables are not in the PATH, it will return the missing 
		// environment variables
		if (tfVersion.contains("GPU") && cudaVersion.equals("nocuda")) {
				pnLoad.append("p", "No CUDA distribution found.\n");
				parent.setGPUTf("CPU");
		} else if (tfVersion.contains("GPU") && !cudaVersion.contains(File.separator) && !cudaVersion.contains("---")) {
			pnLoad.append("p", "Currently using CUDA " + cudaVersion);
			pnLoad.append("p", DeepLearningModel.TensorflowCUDACompatibility(tfVersion, cudaVersion));
		} else if (tfVersion.contains("GPU") && !cudaVersion.contains(File.separator) && cudaVersion.contains("---")) {
			// In linux several CUDA versions are allowed. These versions will be separated by "---"
			String[] versions = cudaVersion.split("---");
			if (versions.length == 1) {
				pnLoad.append("p", "Currently using CUDA " + versions[0]);
			} else {
				for (String str : versions)
					pnLoad.append("p", "Found CUDA " + str);
			}
			pnLoad.append("p", DeepLearningModel.TensorflowCUDACompatibility(tfVersion, cudaVersion));
		} else if (tfVersion.contains("GPU") && (cudaVersion.contains("bin") || cudaVersion.contains("libnvvp"))) {
			pnLoad.append("p", DeepLearningModel.TensorflowCUDACompatibility(tfVersion, cudaVersion));
			String[] outputs = cudaVersion.split(";");
			pnLoad.append("p", "Found CUDA distribution " + outputs[0] + ".\n");
			pnLoad.append("p", "Could not find environment variable:\n - " + outputs[1] + "\n");
			if (outputs.length == 3)
				pnLoad.append("p", "Could not find environment variable:\n - " + outputs[2] + "\n");
			pnLoad.append("p", "Please add the missing environment variables to the path.\n");
		}
		
		pnLoad.append("h2", "Model info");
		File file = new File(params.path2Model);
		if (file.exists())
			name = file.getName();

		pnLoad.append("h2", "Load " + name);

		String pnTxt = pnLoad.getText();
		Log log = new Log();
		params.tag = null;
		
		// Block back button while loading
		parent.setEnabledBackNext(false);
		Object[] info = null;
		double time = -1;
		pnLoad.append("<p>Loading model...");
		ArrayList<String> initialSmi = null;
		ArrayList<String> finalSmi = null;
		try {
			if (tfVersion.contains("GPU") && parent.getGPUTf().equals(""))
				initialSmi = SystemUsage.runNvidiaSmi();
			double chrono = System.nanoTime();
			info = DeepLearningModel.findTfTag(params.path2Model);
			time = System.nanoTime() - chrono;
			if (tfVersion.contains("GPU") && parent.getGPUTf().equals(""))
				finalSmi = SystemUsage.runNvidiaSmi();
		} catch (Exception ex) {
			pnLoad.clear();
			pnLoad.setText(pnTxt);
			ex.printStackTrace();
			IJ.error("DeepImageJ could not load the model,"
					+ "try with another Tensorflow version");
			pnLoad.append("h2", "DeepImageJ could not load the model.\n");
			pnLoad.append("h2", "Try with another Tensorflow version.\n");
			// Let the developer go back, but no forward
			parent.setEnabledBack(true);
			parent.setEnabledNext(false);
			return;
		}
		pnLoad.append(" -> Loaded!!!</p>");
		
		// Check if the model has been loaded on GPU
		if (tfVersion.contains("GPU") && !parent.getGPUTf().equals("GPU")) {
			String GPUInfo = SystemUsage.isUsingGPU(initialSmi, finalSmi);
			// TODO if the CUDA version is not compatible with the TF version,
			// it is impossible to load the model on GPU
			if (GPUInfo.equals("noImageJProcess") && !cudaVersion.contains(File.separator)) {
				pnLoad.append("p", "Unable to run nvidia-smi to check if the model was loaded on a GPU.\n");
				parent.setGPUTf("???");
			} else if (GPUInfo.equals("noImageJProcess")) {
				pnLoad.append("p", "Unable to load model on GPU.\n");
				parent.setGPUTf("CPU");
			} else if(GPUInfo.equals("¡RepeatedImageJGPU!")) {
				int nImageJInstances = SystemUsage.numberOfImageJInstances();
				// Get number of IJ instances using GPU
				int nGPUIJInstances = GPUInfo.split("¡RepeatedImageJGPU!").length;
				if (nImageJInstances > nGPUIJInstances) {
					pnLoad.append("p", "Found " + nGPUIJInstances + "instances of ImageJ/Fiji using GPU"
							+ " out of the " + nImageJInstances + " opened.\n");
					pnLoad.append("p", "Could not assert that the model was loaded on the <b>GPU</b>.\n");
					parent.setGPUTf("???");
				} else if (nImageJInstances <= nGPUIJInstances) {
					pnLoad.append("p", "Model loaded on the <b>GPU</b>.\n");
					if (cudaVersion.contains("bin") || cudaVersion.contains("libnvvp"))
						pnLoad.append("p", "Note that with missing environment variables, GPU performance might not be optimal.\n");
					parent.setGPUTf("GPU");
				}
			} else {
				pnLoad.append("p", "Model loaded on the <b>GPU</b>.\n");
				if (cudaVersion.contains("bin") || cudaVersion.contains("libnvvp"))
					pnLoad.append("p", "Note that due to missing environment variables, GPU performance might not be optimal.\n");
				parent.setGPUTf("GPU");
			}
		} else if (tfVersion.contains("GPU")) {
			pnLoad.append("p", "Model loaded on the <b>GPU</b>.\n");
			if (cudaVersion.contains("bin") || cudaVersion.contains("libnvvp"))
				pnLoad.append("p", "Note that due to missing environment variables, GPU performance might not be optimal.\n");
		}
		
		String tag = (String) info[0];
		if (tag != null) {
			params.tag = tag;
			String tfTag = DeepLearningModel.returnTfTag(tag);
			cmbTags.addItem(tfTag);
			cmbTags.setEditable(false);
			SavedModelBundle model = null;
			if (!(info[2] instanceof SavedModelBundle)) {
				model = DeepLearningModel.loadTf(params.path2Model, params.tag, log);
			} else {
				model = (SavedModelBundle) info[2];
				addLoadInfo(params, time);
			}
			parent.getDeepPlugin().setTfModel(model);
			try {
				params.graphSet = DeepLearningModel.metaGraphsSet(model);
			} catch (Exception ex) {
				ex.printStackTrace();
				IJ.error("DeepImageJ could not load the model,\n"
						+ "try with another Tensorflow version");
				pnLoad.append("h2", "DeepImageJ could not load the model.\n");
				pnLoad.append("h2", "Try with another Tensorflow version.\n");
				// Let the developer go back, but no forward
				parent.setEnabledBack(true);
				parent.setEnabledNext(false);
				return;
			}
			if (params.graphSet.size() > 0) {
				Set<String> tfGraphSet = DeepLearningModel.returnTfSig(params.graphSet);
				for (int i = 0; i < params.graphSet.size(); i++) {
					cmbGraphs.addItem((String) tfGraphSet.toArray()[i]);
					cmbGraphs.setEditable(false);
				}
			}
		} else {
			cmbTags.addItem("");
			cmbTags.setEditable(true);
			cmbGraphs.addItem("");
			cmbGraphs.setEditable(false);
			pnLoad.append("p", "The plugin could not load the model automatically,<br>"
					+ "please introduce the needed information to load the model.");
		}
		// If we loaded either a Bioimage Zoo or Tensoflow model we continue
		parent.setEnabledBackNext(true);
	}
	
	/*
	 * Check if the classes 
	 */

	/*
	 * Add load information to the panel
	 */
	private void addLoadInfo(Parameters params, double time) {
		pnLoad.append("p", "Path to model: " + params.path2Model + "\n");
		String timeStr = (time / 1000000000) + "";
		timeStr = timeStr.substring(0, timeStr.lastIndexOf(".") + 3);
		pnLoad.append("p", "Time to load model: " + timeStr + " s\n");
		String modelSize = "" + FileTools.getFolderSize(params.path2Model + File.separator + "variables") / (1024*1024.0);
		modelSize = modelSize.substring(0, modelSize.lastIndexOf(".") + 3);
		pnLoad.append("p", "Size of the weights: " + modelSize + " MB");
		
	} 
}
