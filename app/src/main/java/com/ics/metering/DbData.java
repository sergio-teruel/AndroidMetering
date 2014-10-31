package com.ics.metering;

import java.lang.reflect.Field;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class DbData {

    //Municipios
    public static class City{
        int id;
        String name;
        //Constructor
        public City(int id, String name) {
            super();
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
        public int getId() {
            return id;
        }
    }    

    //Libros
    public static class Book{
        int id;
        String name;
        int city_id;
        //Constructor
        public Book(int id, String name) {
            super();
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
        public int getId() {
            return id;
        }
    }    

    //Calles
    public static class Street{
        int id;
        String name;
        int value;
        int supply_point_unread;
        
        //Constructor
        public Street(int id, String name, int supply_point_unread) {
            super();
            this.id = id;
            this.name = name;
            this.value= supply_point_unread; 
            this.supply_point_unread= supply_point_unread; 
        }
        @Override
        public String toString() {
            return name;
        }
        public int getId() {
            return id;
        }
    }
    
    //Acometidas
    public static class SupplyPoint{
        int supply_point_id;
        int meter_id;
        int city_id;
        int book_id;
        int street_id;
        int partner_id;
        int contract_id;

        //Constructor
        public SupplyPoint(Object[] fields){
            super();
            //Constructor userConstructor = this  getConstructor(new Class[] {String.class, String.class, Integer.class});
        }
    }

    public static class Recordset{
        Object table;
        public Recordset(Object table){
            super();
            this.table = table;
        }
    }

    public static void LoadField(Class clase){
        for (Field campo : clase.getDeclaredFields()) {
        }
    }
    
    public static String CreateTable(Class clase){
    	int i = 0;
    	int n = clase.getDeclaredFields().length;
        String sql = "CREATE TABLE '" + clase.getSimpleName() + "' (";
        Field[] x = clase.getDeclaredFields();
        for (Field campo : clase.getDeclaredFields()) {
            sql += " '" + campo.getName() +"'";
            sql += " " + sqLiteType(campo);
            if (campo.getName().equals("id")){
            	sql += " PRIMARY KEY  NOT NULL  UNIQUE";
            }
            i++;
            if (i < n){
                sql += ",";
            } else {
            	sql += ");";
            }
        }
        return sql;
    }public static String sqLiteType(Field field){
    	String type = field.getGenericType().toString();
    	if (type.contains("int")){
    		return "INTEGER";
    	}else {
        	if (type.contains("String")){
        		return "TEXT";
        	} else {
            	return field.toString();
        	}
    	}
    }
    
    public static class tableAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
/*    public void ss(){
    adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_2,list){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                TwoLineListItem row;
                if(convertView == null){
                    LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem)inflater.inflate(android.R.layout.simple_list_item_2, null);
                }else{
                    row = (TwoLineListItem)convertView;
                }
                BasicNameValuePair data = list.get(position);
                row.getText1().setText(data.getName());
                row.getText2().setText(data.getValue());
                return row;
            }
        };
    
    listView.setAdapter(adapter);
    }
*/
}
