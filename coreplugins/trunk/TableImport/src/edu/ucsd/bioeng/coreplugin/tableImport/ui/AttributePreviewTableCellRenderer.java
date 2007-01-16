package edu.ucsd.bioeng.coreplugin.tableImport.ui;

import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.ALIAS_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.EDGE_ATTR_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.HEADER_BACKGROUND_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.HEADER_UNSELECTED_BACKGROUND_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.INTERACTION_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.NOT_SELECTED_COL_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.ONTOLOGY_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.PRIMARY_KEY_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.SELECTED_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.SOURCE_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.SPECIES_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.TARGET_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.UNSELECTED_COLOR;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogFontTheme.SELECTED_COL_FONT;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogFontTheme.SELECTED_FONT;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogFontTheme.UNSELECTED_FONT;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.BOOLEAN_ICON;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.CHECKED_ICON;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.FLOAT_ICON;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.INTEGER_ICON;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.LIST_ICON;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.STRING_ICON;
import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.UNCHECKED_ICON;

import static edu.ucsd.bioeng.coreplugin.tableImport.reader.TextFileDelimiters.*;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import cytoscape.data.CyAttributes;

/**
 * Cell and table header renderer for preview table.
 * 
 * @author kono
 * 
 */
public class AttributePreviewTableCellRenderer extends DefaultTableCellRenderer {

	public static final int PARAMETER_NOT_EXIST = -1;
	private final static String DEF_LIST_DELIMITER = PIPE.toString();

	private int keyInFile;
	private List<Integer> aliases;
	private int ontologyColumn;
	private int species;
	private boolean[] importFlag;
	private String listDelimiter;

	/*
	 * For network import
	 */
	private int source;
	private int target;
	private int interaction;

	/*
	 * Constructors.<br>
	 * 
	 * Primary Key is required.
	 */

	public AttributePreviewTableCellRenderer(int primaryKey,
			List<Integer> aliases, final String listDelimiter) {
		this(primaryKey, aliases, PARAMETER_NOT_EXIST, PARAMETER_NOT_EXIST,
				null, listDelimiter);
	}

	public AttributePreviewTableCellRenderer(int primaryKey,
			List<Integer> aliases, int ontologyColumn) {
		this(primaryKey, aliases, ontologyColumn, PARAMETER_NOT_EXIST, null,
				DEF_LIST_DELIMITER);
	}

	public AttributePreviewTableCellRenderer(int primaryKey,
			List<Integer> aliases, int ontologyColumn, int species) {
		this(primaryKey, aliases, ontologyColumn, species, null,
				DEF_LIST_DELIMITER);
	}

	public AttributePreviewTableCellRenderer(int primaryKey,
			List<Integer> aliases, int ontologyColumn, int species,
			boolean[] importFlag, final String listDelimiter) {
		super();
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		this.source = PARAMETER_NOT_EXIST;
		this.target = PARAMETER_NOT_EXIST;
		this.interaction = PARAMETER_NOT_EXIST;

		this.keyInFile = primaryKey;
		this.ontologyColumn = ontologyColumn;

		if (aliases == null) {
			this.aliases = new ArrayList<Integer>();
		} else {
			this.aliases = aliases;
		}

		this.species = species;
		if (importFlag != null) {
			this.importFlag = importFlag;
		}

		if (listDelimiter == null) {
			this.listDelimiter = DEF_LIST_DELIMITER;
		} else {
			this.listDelimiter = listDelimiter;
		}
	}

	public void setImportFlag(int index, boolean flag) {
		if (importFlag != null && importFlag.length > index) {
			importFlag[index] = flag;
		}
	}

	public boolean getImportFlag(int index) {
		if (importFlag != null && importFlag.length > index) {
			return importFlag[index];
		}
		return false;
	}

	public void setAliasFlag(Integer i, boolean flag) {
		if (aliases.contains(i) && flag == false) {
			aliases.remove(i);
		} else if (!aliases.contains(i) && flag == true) {
			aliases.add(i);
		}
	}

	public void setSourceIndex(int idx) {
		source = idx;
	}

	public void setInteractionIndex(int idx) {
		interaction = idx;
	}

	public void setTargetIndex(int idx) {
		target = idx;
	}

	public int getSourceIndex() {
		return source;
	}

	public int getInteractionIndex() {
		return interaction;
	}

	public int getTargetIndex() {
		return target;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

		if (importFlag == null
				|| table.getModel().getColumnCount() != importFlag.length) {
			importFlag = new boolean[table.getColumnCount()];
			for (int i = 0; i < importFlag.length; i++) {
				importFlag[i] = true;
			}
		}

		if (column == keyInFile) {
			setForeground(PRIMARY_KEY_COLOR.getColor());
		} else if (column == ontologyColumn) {
			setForeground(ONTOLOGY_COLOR.getColor());
		} else if (aliases.contains(column)) {
			setForeground(ALIAS_COLOR.getColor());
		} else if (column == species) {
			setForeground(SPECIES_COLOR.getColor());
		} else if (column == source) {
			setForeground(SOURCE_COLOR.getColor());
			importFlag[column] = true;
		} else if (column == target) {
			setForeground(TARGET_COLOR.getColor());
			importFlag[column] = true;
		} else if (column == interaction) {
			setForeground(INTERACTION_COLOR.getColor());
			importFlag[column] = true;
		} else if (column != source && column != target
				&& column != interaction && source != PARAMETER_NOT_EXIST
				&& importFlag[column] == true) {
			setForeground(EDGE_ATTR_COLOR.getColor());
		} else {
			setForeground(Color.BLACK);
		}

		setText((value == null) ? "" : value.toString());

		if (importFlag[column] == true) {
			setBackground(Color.WHITE);
			setFont(SELECTED_COL_FONT.getFont());
		} else {
			setBackground(NOT_SELECTED_COL_COLOR.getColor());
			setFont(table.getFont());
		}
		return this;
	}

}

/**
 * For rendering table header.
 * 
 * @author kono
 * 
 */
class HeaderRenderer implements TableCellRenderer {

	private static final int PARAMETER_NOT_EXIST = -1;
	private final TableCellRenderer tcr;

	public HeaderRenderer(TableCellRenderer tcr, Byte[] dataTypes) {
		this.tcr = tcr;
	}

	public Component getTableCellRendererComponent(JTable tbl, Object val,
			boolean isS, boolean hasF, int row, int col) {
		final JLabel columnName = (JLabel) tcr.getTableCellRendererComponent(
				tbl, val, isS, hasF, row, col);
		final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) tbl
				.getCellRenderer(0, col);
		final boolean flag = rend.getImportFlag(col);
		final int source = rend.getSourceIndex();
		final int interaction = rend.getInteractionIndex();
		final int target = rend.getTargetIndex();

		if (flag) {
			columnName.setFont(SELECTED_FONT.getFont());
			columnName.setForeground(SELECTED_COLOR.getColor());
			columnName.setBackground(HEADER_BACKGROUND_COLOR.getColor());
		} else {
			columnName.setFont(UNSELECTED_FONT.getFont());
			columnName.setForeground(UNSELECTED_COLOR.getColor());
			columnName.setBackground(HEADER_UNSELECTED_BACKGROUND_COLOR
					.getColor());
		}

		if (col == source) {
			columnName.setFont(SELECTED_FONT.getFont());
			columnName.setForeground(SOURCE_COLOR.getColor());
			columnName.setBackground(HEADER_BACKGROUND_COLOR.getColor());
		} else if (col == target) {
			columnName.setFont(SELECTED_FONT.getFont());
			columnName.setBackground(HEADER_BACKGROUND_COLOR.getColor());
			columnName.setForeground(TARGET_COLOR.getColor());
		} else if (col == interaction) {
			columnName.setFont(SELECTED_FONT.getFont());
			columnName.setBackground(HEADER_BACKGROUND_COLOR.getColor());
			columnName.setForeground(INTERACTION_COLOR.getColor());
		} else if (col != target && col != source && col != interaction
				&& source != PARAMETER_NOT_EXIST && flag == true) {
			columnName.setForeground(EDGE_ATTR_COLOR.getColor());
		}

		if (flag || source == col || target == col || interaction == col) {
			columnName.setIcon(CHECKED_ICON.getIcon());
		} else {
			columnName.setIcon(UNCHECKED_ICON.getIcon());
		}

		return columnName;
	}

	private static ImageIcon getDataTypeIcon(byte dataType) {
		ImageIcon dataTypeIcon = null;
		if (dataType == CyAttributes.TYPE_STRING) {
			dataTypeIcon = STRING_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_INTEGER) {
			dataTypeIcon = INTEGER_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_FLOATING) {
			dataTypeIcon = FLOAT_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_BOOLEAN) {
			dataTypeIcon = BOOLEAN_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_SIMPLE_LIST) {
			dataTypeIcon = LIST_ICON.getIcon();
		}
		return dataTypeIcon;
	}

}
