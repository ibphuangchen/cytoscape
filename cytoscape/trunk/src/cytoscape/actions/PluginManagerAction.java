/*
 File: PluginManagerAction.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

// EdgeControlDialog
//---------------------------------------------------------------------------------------
// $Revision: 9565 $
// $Date: 2007-02-13 11:36:50 -0800 (Tue, 13 Feb 2007) $
// $Author: mes $
//---------------------------------------------------------------------------------------
package cytoscape.actions;

import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;

import cytoscape.bookmarks.Bookmarks;
import cytoscape.bookmarks.Category;
import cytoscape.bookmarks.DataSource;

import cytoscape.dialogs.plugins.PluginManageDialog;
import cytoscape.dialogs.plugins.PluginManageDialog.PluginInstallStatus;

import cytoscape.plugin.PluginManager;
import cytoscape.plugin.PluginInquireAction;
import cytoscape.plugin.PluginInfo;
import cytoscape.plugin.ManagerUtil;
import cytoscape.plugin.PluginTracker.PluginStatus;

import cytoscape.util.BookmarksUtil;
import cytoscape.util.CytoscapeAction;

import java.awt.event.ActionEvent;

import java.util.List;
import java.util.Map;

public class PluginManagerAction extends CytoscapeAction {
	private String bookmarkCategory = "plugins";

	public PluginManagerAction() {
		super("Manage Plugins");
		setPreferredMenu("Plugins");
	}

	/**
	 * Gets the lists of plugins and creates the ManagerDialog for users.
	 * 
	 * @param e
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		PluginManageDialog dialog = new PluginManageDialog(Cytoscape
				.getDesktop());
		PluginManager Mgr = PluginManager.getPluginManager();

		String DefaultUrl = "";
		String DefaultTitle = "";

		try {
			Bookmarks theBookmarks = Cytoscape.getBookmarks();
			// Extract the URL entries
			List<DataSource> DataSourceList = BookmarksUtil.getDataSourceList(
					bookmarkCategory, theBookmarks.getCategory());

			for (DataSource ds : DataSourceList) {
				if (ds.getName().equals("Cytoscape")) {
					DefaultUrl = ds.getHref();
					DefaultTitle = ds.getName();
					break;
				}
			}
		} catch (Exception E) {
			//E.printStackTrace();
			System.err.println("There was an error while reading the bookmarks file.");
		}

		List<PluginInfo> Current = Mgr.getPlugins(PluginStatus.CURRENT);
		Map<String, List<PluginInfo>> InstalledInfo = ManagerUtil
				.sortByCategory(Current);

		for (String Category : InstalledInfo.keySet()) {
			dialog.addCategory(Category, InstalledInfo.get(Category),
					PluginInstallStatus.INSTALLED);
		}

		Mgr.runThreadedInquiry(DefaultUrl, new ManagerAction(dialog,
				DefaultTitle, DefaultUrl));
	}

	private class ManagerAction extends PluginInquireAction {

		private PluginManageDialog dialog;
		private String title;
		private String url;

		public ManagerAction(PluginManageDialog Dialog, String Title, String Url) {
			dialog = Dialog;
			title = Title;
			url = Url;
		}

		public boolean displayProgressBar() {
			return true;
		}
		
		public String getProgressBarMessage() {
			return "Attempting to connect to " + url;
		}

		public void inquireAction(List<PluginInfo> Results) {

			if (isExceptionThrown()) {
				if (getIOException() != null) {
					// failed to read the given url
					dialog.setMessage(PluginManageDialog.CommonError.NOXML
							+ url);
				} else if (getJDOMException() != null) {
					// failed to parse the xml file at the url
					dialog.setMessage(PluginManageDialog.CommonError.BADXML
							+ url);
				} else {
					dialog.setMessage(getException().getMessage());
				}
			} else {
				Map<String, List<PluginInfo>> DownloadInfo = ManagerUtil
						.sortByCategory(Results);

				for (String Category : DownloadInfo.keySet()) {
					// get only the unique ones
					dialog.addCategory(Category, DownloadInfo.get(Category),
							PluginInstallStatus.AVAILABLE);
				}
			}
			dialog.setSiteName(title);
			dialog.setVisible(true);
		}
	}

}
