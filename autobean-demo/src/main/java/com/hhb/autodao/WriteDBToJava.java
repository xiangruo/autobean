package com.hhb.autodao;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class WriteDBToJava {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WriteDBToJava.class);
	
	private static String pakg;
	private static String url = "E:/temp_code/";
	private static String subUrl1 = "pojo";
	private static String subUrl2 = "mybatis";
	private static String subUrl3 = "dao";
	private static String rowNum = "10";
	
	public static void init(String package_name,String filePath){
		pakg = package_name;
		if(filePath != null && filePath.length() > 0){
			url = filePath;
		}
	}
	
	
	public static void writeJavaBean(Map<String,List<Colm>> mTables){
		
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Map<String,String> imports = new HashMap<String,String>();
			try {
				File tmp = new File(url);
				if(!tmp.isDirectory()){
					tmp.mkdir();
				}
				File f = new File(url+subUrl1+"/");
				f.mkdir();
				
				f = new File(url+subUrl1+"/"+key+".java");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				output.write("package "+pakg+subUrl1+";\n");
				output.write("\n");
				
				for (Colm colm : l) {
					if(imports.get(colm.getTypeImport()) == null && !"java.lang.String".equals(colm.getTypeImport())){
						output.write("import "+colm.getTypeImport()+";");
						output.write("\n");
						imports.put(colm.getTypeImport(), colm.getTypeImport());
					}
				}
				output.write("\n");
				output.write("public class "+key+" {\n\n");
				StringBuffer line;
				for (Colm colm : l) {
					line = new StringBuffer();
					line.append("\t ");
					line.append("private ");
					line.append(colm.getTypeName() + " ");
					LOGGER.info("xxxxxxxxxxxxxx:"+colm.getComment());
					line.append(colm.getName() + "; //"+colm.getComment()+"\n\n");
					output.write(line.toString());
				}
				for (Colm colm : l) {
					//set
					line = new StringBuffer();
					line.append("\t\t");
					line.append("public ");
					line.append(key+" ");
					line.append("set"+ AutoBean.toU(colm.getName().charAt(0)) + colm.getName().substring(1, colm.getName().length()));
					line.append(" ("+colm.getTypeName()+" "+colm.getName()+") {\n");
					output.write(line.toString());
					
					line = new StringBuffer();
					line.append("\t\t\tthis."+colm.getName()+" = "+colm.getName());
					line.append(";\n");
					output.write(line.toString());
					
					line = new StringBuffer();
					line.append("\t\t\treturn this");
					line.append(";\n");
					output.write(line.toString());
					
					line = new StringBuffer();
					line.append("\t\t}");
					line.append("\n\n");
					output.write(line.toString());
					
					//get
					line = new StringBuffer();
					line.append("\t\t");
					line.append("public ");
					line.append(colm.getTypeName()+" ");
					line.append("get"+ AutoBean.toU(colm.getName().charAt(0)) + colm.getName().substring(1, colm.getName().length()));
					line.append(" () {\n");
					output.write(line.toString());
					
					line = new StringBuffer();
					line.append("\t\t\treturn "+colm.getName());
					line.append(";\n");
					output.write(line.toString());
					
					
					line = new StringBuffer();
					line.append("\t\t}");
					line.append("\n\n");
					output.write(line.toString());
				}
				output.write("}\n");
				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
	}
	
	
	/**
	 * 
	 * Description: 生成mybatis文件
	 * @Version1.0 2014-3-12 上午9:23:07 by jk（jiake@ucfgroup.com）创建
	 * @param mTables
	 */
	public static void writeXML(Map<String,List<Colm>> mTables){
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			//System.out.println("<typeAlias type=\""+pakg+"pojo."+key+"\" alias=\""+AutoBean.toL(key.charAt(0))+key.substring(1)+"\" />");
		//	Map<String,String> imports = new HashMap<String,String>();
			try {
				File f = new File(url+subUrl2+"/");
				f.mkdir();
				f = new File(url+subUrl2+"/"+AutoBean.jNameToDbName(key).toLowerCase()+"_sql.xml");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
				output.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
				//增加了类名
				output.write("<mapper namespace=\"" + pakg.substring(0, pakg.length()-1) + "."+key+"Dao\">\n");
				
				
				//插入操作
				output.write("<!-- insert "+key+" -->\n");
				output.write("<insert id=\"insert"+key+"\" parameterType=\""+key+"\" keyProperty=\"id\" useGeneratedKeys=\"true\">\n");
				output.write("\t<![CDATA[\n");
				output.write("\t\tINSERT INTO " + AutoBean.jNameToDbName(key) + " (\n");
				StringBuffer sb1 = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				for (int i = 0; i < l.size(); i++) {
					if(i>0){
						sb1.append(",");
						sb2.append(",");
					}
					Colm c = l.get(i);
					sb1.append(c.getDbName());
					//去掉特殊处理
//					if(i == 0){
//						sb2.append(AutoBean.jNameToDbName(key)+"_SEQ.nextval");
//					}else{
//						sb2.append("#{"+c.getName()+",jdbcType="+AutoBean.typeDbToJava2(c.getType())+"}");
//					}
					sb2.append("#{"+c.getName()+",jdbcType="+AutoBean.typeDbToJava2(c.getType())+"}");

				}
				output.write("\t"+sb1.toString()+"\n");
				output.write("\t\t)VALUES(\n");
				output.write("\t\t"+sb2.toString());
				output.write(")\n\t]]>\n</insert>\n");
				output.write("\n");
				
				
				//修改操作
				output.write("<!-- update "+key+" -->\n");
				//去掉驼峰
				output.write("<update id=\"update"+key+"\" parameterType=\""+key+"\">\n");
//				output.write("<update id=\"update"+key+"\" parameterType=\""+AutoBean.toL(key.charAt(0))+key.substring(1)+"\">\n");
				output.write("\t<![CDATA[\n");
				output.write("\t\tUPDATE \n");
				output.write("\t\t" + AutoBean.jNameToDbName(key) + " \n");
				output.write("\t\tSET \n");
				Colm cl = l.get(0);
				output.write("\t\t "+cl.getDbName() + "=" +cl.getDbName()+"\n");
				output.write("\t]]>\n");
				StringBuffer sb3 = new StringBuffer("\t\t");
				for (int i = 1; i < l.size(); i++) {
					Colm c = l.get(i);
					
					//空的字段不做修改操作...
					//if(c.getTypeName().equals("String")){
						//sb3.append("\t<if test=\"null != "+c.getName()+" and '' != "+c.getName()+"\">\n");
					//}else{
						sb3.append("\t<if test=\"null != "+c.getName()+"\">\n");
					//}
					sb3.append("\t<![CDATA[\n");
					sb3.append(",");
					sb3.append(c.getDbName());
					sb3.append(" = #{");
					sb3.append(c.getName());
					sb3.append("}");
					sb3.append("\n\t]]>\n");
					sb3.append("\t</if>\n");
				}
				sb3.append("\t<![CDATA[\n");
				sb3.append("\n");
				output.write(sb3.toString());
				output.write("\t\tWHERE  "+cl.getDbName()+" = #{"+cl.getName()+"} \n");
				output.write("\t]]>\n");
				output.write("</update>\n\n");
				
				
				//删除操作
				output.write("<!-- delete "+ key +" -->\n");
				output.write("<delete id=\"delete"+ key +"\" parameterType=\"string\">\n");
				output.write("\t<![CDATA[\n");
				output.write("\t\tDELETE FROM " + AutoBean.jNameToDbName(key) + " WHERE id = #{id}\n");
				output.write("\t]]>\n");
				output.write("</delete>\n");
				output.write("\n");
				
				
				//查询操作
				output.write("<!-- select "+ key +" -->\n");
				output.write("<select id=\"query"+key+"ByWhere\" resultType=\""+key+"\" parameterType=\""+key+"\" >\n");
				output.write("<![CDATA[\n");
				output.write("SELECT \n");
				StringBuffer sb4 = new StringBuffer();
				for (int i = 0; i < l.size(); i++) {
					if(i>0){
						sb4.append(",");
					}
					Colm cc = l.get(i);
					sb4.append(cc.getDbName());
					sb4.append(" as ");
					sb4.append(cc.getName());
				}
				output.write(sb4.toString());
//				output.write(" FROM "+AutoBean.jNameToDbName(key)+" WHERE ROWNUM <= "+rowNum+" \n");
				output.write(" FROM "+AutoBean.jNameToDbName(key)+" WHERE 1=1 \n");
				output.write("]]>\n");
				for (Colm colm : l) {
					if(colm.getTypeName().equals("String")){
						output.write("<if test=\"null != "+colm.getName()+" and '' != "+colm.getName()+"\">\n");
					}else{
						output.write("<if test=\"null != "+colm.getName()+"\">\n");
					}
					output.write("<![CDATA[\n");
					output.write("AND "+ colm.getDbName() +" = #{"+colm.getName()+"}\n");
					output.write("]]>\n");
					output.write("</if>\n");
				}
				output.write("</select>\n");
				output.write("</mapper>\n");
				
				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
	}
	
	
	
	/**
	 * 添加系统异常引入
	 * Description: 生成DAO接口文件
	 * @Version1.0 2014-3-12 上午9:17:24 by jk（jiake@ucfgroup.com）创建
	 * @param mTables
	 */
	public static void writeDAO(Map<String,List<Colm>> mTables){
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
		//	Map<String,String> imports = new HashMap<String,String>();
			try {
				File f = new File(url+subUrl3+"/");
				f.mkdir();
				f = new File(url+subUrl3+"/"+key+"Dao.java");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				String s = AutoBean.toL(key.charAt(0))+key.substring(1, key.length());
				output.write("package "+pakg+subUrl3+";\n\n");
				output.write("import java.io.Serializable;\n");
				output.write("import java.util.List;\n");
				output.write("import "+pakg+subUrl1+"."+key+";\n");
				//output.write("import com.ucf.onlinepay.framework.exception.in.FnFiTechnicalException;\n\n");
				
				output.write("@Repository(\""+AutoBean.toL(key.charAt(0))+key.substring(1)+"Dao\")\n");
				output.write("public interface "+key+"Dao extends Serializable {\n\n");
				output.write("\tvoid insert"+key+"("+key+" "+s+")throws Exception;\n\n");
				output.write("\tvoid update"+key+"("+key+" "+s+")throws Exception;\n\n");
				output.write("\tvoid delete"+key+"(String "+l.get(0).getName()+")throws Exception;\n\n");
//				output.write("\tList<"+key+"> query"+key+"ByWhere("+key+" "+s+")throws Exception;\n\n");
				output.write("\tpublic "+key+" query"+key+"ByWhere("+key+" "+s+")throws Exception;\n");
				output.write("}");
				output.close();
			}catch (IOException e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
	}
	
	
	/**
	 * 添加系统异常引入
	 * Description: 生成DAO文件
	 * @Version1.0 2014-3-12 上午9:18:44 by jk（jiake@ucfgroup.com）创建
	 * @param mTables
	 */
	public static void writeDAOImpl(Map<String,List<Colm>> mTables){
		Set<String> set = mTables.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
		//	Map<String,String> imports = new HashMap<String,String>();
			try {
				File f = new File(url+subUrl3+"/impl/");
				f.mkdir();
				f = new File(url+subUrl3+"/impl/"+key+"DaoImpl.java");
				f.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				List<Colm> l = mTables.get(key);
				String s = AutoBean.toL(key.charAt(0))+key.substring(1, key.length());
				output.write("package "+pakg+subUrl3+".impl;\n\n");
				output.write("import org.springframework.stereotype.Repository;\n");
				output.write("import java.util.List;\n");
				output.write("import "+pakg+subUrl1+"."+key+";\n");
				output.write("import com.ucf.onlinepay.framework.dao.BaseDao;\n");
				output.write("import "+pakg+subUrl3+"."+key+"Dao;\n");
				//output.write("import com.ucf.onlinepay.framework.exception.in.FnFiTechnicalException;\n\n");
				output.write("@Repository\n");
				output.write("public class "+key+"DaoImpl extends BaseDao implements "+key+"Dao {\n\n");
				output.write("private static final long serialVersionUID = -1L;\n\n");
				output.write("\tpublic void insert"+key+"("+key+" "+s+")throws Exception{\n");
				output.write("\t\tthis.insert(\""+pakg+"insert"+key+"\", "+s+");\n");
				output.write("\t}\n\n");
				output.write("\tpublic void update"+key+"("+key+" "+s+")throws Exception{\n");
				output.write("\t\tthis.update(\""+pakg+"update"+key+"\", "+s+");\n");
				output.write("\t}\n\n");
				output.write("\tpublic void delete"+key+"(String "+l.get(0).getName()+")throws Exception{\n");
				output.write("\t\tthis.delete(\""+pakg+"delete"+key+"\", "+l.get(0).getName()+");\n");
				output.write("\t}\n\n");
				output.write("\tpublic List<"+key+"> query"+key+"ListByWhere("+key+" "+s+")throws Exception{\n");
				output.write("\t\treturn this.queryForList(\""+pakg+"query"+key+"ByWhere\", "+s+");\n");
				output.write("\t}\n\n");
				output.write("}");
				output.close();
			}catch (IOException e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
	}
	

}
