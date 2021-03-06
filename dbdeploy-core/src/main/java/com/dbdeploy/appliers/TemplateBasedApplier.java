package com.dbdeploy.appliers;

import com.dbdeploy.exceptions.UsageException;
import com.dbdeploy.scripts.ChangeScript;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TemplateBasedApplier implements ChangeScriptApplier {
	private Configuration configuration;
	private Writer outputFileWriter;
	private String syntax;
	private String changeLogTableName;

	public TemplateBasedApplier(File outputfile, String syntax, String changeLogTableName, File templateDirectory) throws IOException {
		this.syntax = syntax;
		this.changeLogTableName = changeLogTableName;
		this.outputFileWriter = new PrintWriter(new PrintStream(outputfile));
		this.configuration = new Configuration();

		FileTemplateLoader fileTemplateLoader = createFileTemplateLoader(templateDirectory);
		this.configuration.setTemplateLoader(
				new MultiTemplateLoader(new TemplateLoader[]{
						fileTemplateLoader,
						new ClassTemplateLoader(getClass(), "/"),
				}));
	}

    private FileTemplateLoader createFileTemplateLoader(File templateDirectory) throws IOException {
		if (templateDirectory == null) {
			return new FileTemplateLoader();
		} else {
			return new FileTemplateLoader(templateDirectory, true);
		}
	}

	public void apply(List<ChangeScript> changeScripts, ApplyMode applyMode) {
		String filename = syntax + "_" + getTemplateQualifier(applyMode) + ".ftl";

		try {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("scripts", changeScripts);
			model.put("changeLogTableName", changeLogTableName);

			Template template = configuration.getTemplate(filename);
			template.process(model, outputFileWriter);
		} catch (FileNotFoundException ex) {
			throw new UsageException("Could not find template named " + filename + "\n" +
					"Check that you have got the name of the database syntax correct.", ex);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String getTemplateQualifier(ApplyMode applyMode) {
	    if (applyMode == ApplyMode.UNDO)
	        return "undo";
	    else 
	        return "apply";
	}

}
