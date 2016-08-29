package gui;

import java.sql.SQLException;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import bttf.DBStorage;
import bttf.Partition;

public class DBAccess {
	private DBStorage dbstorage;
	private JFrame mainWindow;
	
	public DBAccess(JFrame mainWindow){
		this.dbstorage = new DBStorage();
		this.mainWindow = mainWindow;
	}
	
	public Partition getPartitionDataFromDB(Partition partition){
		//get saved data from db
		Date project_date = dbstorage.check_feature_model_exists(partition);
		if(project_date != null){
			int answer = JOptionPane.showConfirmDialog(mainWindow.getContentPane(), "There is saved data for this project and feature model from the date "+ String.format("%tD", project_date)+",\nDo you want to load it?", "I can save you some work :)", JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.YES_OPTION){
				boolean correct = dbstorage.load_partition_data(partition);
				if(!correct){
					JOptionPane.showMessageDialog(mainWindow.getContentPane(), "Error retrieving data from database.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return partition;
	}
	
	public boolean saveStateInDB(Partition partition) throws SQLException{
		return dbstorage.save_state(partition);
	}
	
	public void closeConnection(){
		if(dbstorage != null){
        	dbstorage.close_connection();
        }
	}
}
