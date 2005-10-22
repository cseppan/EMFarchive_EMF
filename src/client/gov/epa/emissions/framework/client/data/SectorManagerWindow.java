package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.Sector;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SectorManagerWindow extends ReusableInteralFrame implements
		SectorManagerView {

	private SectorManagerPresenter presenter;

	private SortFilterSelectModel selectModel;

	private EmfTableModel model;

	private JPanel layout;

	private MessagePanel messagePanel;

	private JFrame parentConsole;

	private SortFilterSelectionPanel sortFilterSelectPanel;

	// FIXME: this class needs to be refactored into smaller components
	public SectorManagerWindow(JFrame parentConsole, JDesktopPane desktop) {
		super("Sector Manager", desktop);
		this.parentConsole = parentConsole;
		this.desktop = desktop;

		layout = new JPanel();
		this.getContentPane().add(layout);
	}

	public void clearMessage() {
		messagePanel.clear();
		redoLayout();
	}

	public void display(Sector[] sectors) {
		model = new EmfTableModel(new SectorsTableData(sectors));
		selectModel = new SortFilterSelectModel(model);

		createLayout(parentConsole);
	}

	public void observe(SectorManagerPresenter presenter) {
		this.presenter = presenter;
	}

	private void createLayout(JFrame parentConsole) {
		layout.removeAll();
		sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole,
				selectModel);
		createLayout(layout, sortFilterSelectPanel);

		this.setSize(new Dimension(550, 300));
	}

	private void createLayout(JPanel layout, JPanel sortFilterSelectPanel) {
		layout.setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
		sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

		messagePanel = new SingleLineMessagePanel();
		layout.add(messagePanel, BorderLayout.NORTH);
		layout.add(scrollPane, BorderLayout.CENTER);
		layout.add(createControlPanel(), BorderLayout.SOUTH);
	}

	private JPanel createControlPanel() {
		JPanel crudPanel = createCrudPanel();

		JPanel closePanel = new JPanel();
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// presenter.doCloseView();
			}
		});
		closePanel.add(closeButton);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());

		controlPanel.add(crudPanel, BorderLayout.WEST);
		controlPanel.add(closePanel, BorderLayout.EAST);

		return controlPanel;
	}

	private JPanel createCrudPanel() {
		return new JPanel();
	}

	private void redoLayout() {
		super.validate();
	}

}
