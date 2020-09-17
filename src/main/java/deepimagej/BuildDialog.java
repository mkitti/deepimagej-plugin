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

package deepimagej;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import deepimagej.components.TitleHTMLPane;
import deepimagej.stamp.InputDimensionStamp;
import deepimagej.stamp.JavaPostprocessingStamp;
import deepimagej.stamp.JavaPreprocessingStamp;
import deepimagej.stamp.LoadPytorchStamp;
import deepimagej.stamp.InformationStamp;
import deepimagej.stamp.LoadTFStamp;
import deepimagej.stamp.OutputDimensionStamp;
import deepimagej.stamp.SaveOutputFilesStamp;
import deepimagej.stamp.SaveStamp;
import deepimagej.stamp.SelectPyramidalStamp;
import deepimagej.stamp.TensorPytorchTmpStamp;
import deepimagej.stamp.TensorStamp;
import deepimagej.stamp.TestStamp;
import deepimagej.stamp.WelcomeStamp;
import deepimagej.tools.Log;
import deepimagej.tools.WebBrowser;
import ij.IJ;
import ij.gui.GUI;

public class BuildDialog extends JDialog implements ActionListener {

	public JButton				bnNext	= new JButton("Next");
	public JButton				bnBack	= new JButton("Back");
	public JButton				bnClose	= new JButton("Cancel");
	public JButton				bnHelp	= new JButton("Help");
	private JPanel				pnCards	= new JPanel(new CardLayout());

	private WelcomeStamp			welcome;
	private LoadTFStamp				loaderTf;
	private LoadPytorchStamp				loaderPt;
	private SelectPyramidalStamp	selectPyramid;
	private InputDimensionStamp		dim3;
	private OutputDimensionStamp 	outputDim;
	private TensorStamp				tensorTf;
	private TensorPytorchTmpStamp	tensorPt;
	private InformationStamp		info;
	private JavaPreprocessingStamp  javaPreproc;
	private JavaPostprocessingStamp  javaPostproc;
	private TestStamp				test2;
	private SaveOutputFilesStamp	outputSelection;
	private SaveStamp				save;
	private DeepImageJ				dp;
	private int						card	= 1;

	public BuildDialog() {
		super(new JFrame(), "Build Bundled Model [" + Constants.version + "]");
	}

	public void showDialog() {

		welcome = new WelcomeStamp(this);
		loaderTf = new LoadTFStamp(this);
		loaderPt = new LoadPytorchStamp(this);
		selectPyramid = new SelectPyramidalStamp(this);
		dim3 = new InputDimensionStamp(this);
		tensorTf = new TensorStamp(this);
		tensorPt = new TensorPytorchTmpStamp(this);
		info = new InformationStamp(this);
		outputDim = new OutputDimensionStamp(this);
		javaPreproc = new JavaPreprocessingStamp(this);
		javaPostproc = new JavaPostprocessingStamp(this);
		test2 = new TestStamp(this);
		outputSelection = new SaveOutputFilesStamp(this);
		save = new SaveStamp(this);

		JPanel pnButtons = new JPanel(new GridLayout(1, 4));
		pnButtons.add(bnClose);
		pnButtons.add(bnHelp);
		pnButtons.add(bnBack);
		pnButtons.add(bnNext);

		pnCards.add(welcome.getPanel(), "1");
		pnCards.add(loaderTf.getPanel(), "2");
		pnCards.add(loaderPt.getPanel(), "20");
		pnCards.add(selectPyramid.getPanel(), "3");
		pnCards.add(tensorTf.getPanel(), "4");
		pnCards.add(tensorPt.getPanel(), "40");
		pnCards.add(dim3.getPanel(), "5");
		pnCards.add(outputDim.getPanel(), "6");
		pnCards.add(info.getPanel(), "7");
		pnCards.add(javaPreproc.getPanel(), "8");
		pnCards.add(javaPostproc.getPanel(), "9");
		pnCards.add(test2.getPanel(), "10");
		pnCards.add(outputSelection.getPanel(), "11");
		pnCards.add(save.getPanel(), "12");

		setLayout(new BorderLayout());
		add(new TitleHTMLPane().getPane(), BorderLayout.NORTH);
		add(pnCards, BorderLayout.CENTER);
		add(pnButtons, BorderLayout.SOUTH);

		bnNext.addActionListener(this);
		bnBack.addActionListener(this);
		bnClose.addActionListener(this);
		bnHelp.addActionListener(this);

		setResizable(false);
		pack();
		GUI.center(this);
		setVisible(true);
		bnBack.setEnabled(false);

	}

	private void setCard(String name) {
		CardLayout cl = (CardLayout) (pnCards.getLayout());
		if (dp.params.framework.equals("Pytorch") && name.equals("2"))
			name = "20";
		if (dp.params.framework.equals("Pytorch") && name.equals("4"))
			name = "40";
		cl.show(pnCards, name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		bnNext.setText("Next");
		bnNext.setEnabled(true);
		if (e.getSource() == bnNext) {
			switch (card) {
			case 1:
				if (welcome.finish()) {
					card = 2;
					String path = welcome.getModelDir();
					String name = welcome.getModelName();
					if (path != null) {
						dp = new DeepImageJ(path, name, new Log(), true);
						if (dp != null) {
							dp.params.path2Model = path + File.separator + name + File.separator;
							if (dp.params.framework.contains("Tensorflow") && dp.getValid()) {
								loaderTf.init();
							} else if (dp.params.framework.contains("Pytorch") && dp.getValid()) {
								loaderPt.init();
							} else if (!dp.getValid()) {
								IJ.error("This directory " + dp.getPath() + " is not a protobuf model (no saved_model.pb) or Torchscript model");
								card = 1;
							}
						}
					}
				}
				break;
			case 2:
				if (dp.params.framework.contains("Tensorflow")) {
					card = loaderTf.finish() ? card+1 : card;
				} else if (dp.params.framework.contains("Pytorch")) {
					card = loaderPt.finish() ? card+1 : card;
				}
				break;
			case 3:
				card = selectPyramid.finish() ? card+1 : card;
				break;
			case 4:
				if (dp.params.framework.contains("Tensorflow")) {
					card = tensorTf.finish() ? card+1 : card;
				} else if (dp.params.framework.contains("Pytorch")) {
					card = tensorPt.finish() ? card+1 : card;
				}
				break;
			case 5:
				card = dim3.finish() ? card+1 : card;
				break;
			case 6:
				card = outputDim.finish() ? card+1 : card;
				break;
			case 7:
				card = info.finish() ? card+1 : card;
				break;
			case 8:
				card = javaPreproc.finish() ? card+1 : card;
				break;
			case 9:
				card = javaPostproc.finish() ? card+1 : card;
				break;
			case 11:
				card = outputSelection.finish() ? card+1 : card;
				break;
			case 12:
				dispose();
			default:
				card = Math.min(12, card + 1);
			}
		}
		if (e.getSource() == bnBack) {
			card = Math.max(1, card - 1);
		}
		if (e.getSource() == bnClose) {
			dispose();
		}
		if (e.getSource() == bnHelp) {
			WebBrowser.openDeepImageJ();
		}

		setCard("" + card);
		bnBack.setEnabled(card > 1);
		if (card == 4 && dp.params.framework.contains("Tensorflow"))
			tensorTf.init();
		if (card == 4 && dp.params.framework.contains("Pytorch"))
			tensorPt.init();
		if (card == 5)
			dim3.init();
		if (card == 6)
			outputDim.init();
		if (card == 7)
			info.init();
		if (card == 8)
			javaPreproc.init();
		if (card == 9)
			javaPostproc.init();
		if (card == 10)
			test2.init();
		if (card == 11)
			outputSelection.init();
		if (card == 12)
			setEnabledBackNext(true);

		bnNext.setText(card == 12 ? "Finish" : "Next");
	}

	public void setEnabledBackNext(boolean b) {
		bnBack.setEnabled(b);
		bnNext.setEnabled(b);
	}

	public void setEnabledNext(boolean b) {
		bnNext.setEnabled(b);
	}

	public void setEnabledBack(boolean b) {
		bnBack.setEnabled(b);
	}

	public void endsTest() {
		bnBack.setEnabled(true);
		bnNext.setEnabled(true);
		bnNext.setText("Next");
	}
	
	public DeepImageJ getDeepPlugin() {
		return dp;
	}
}
