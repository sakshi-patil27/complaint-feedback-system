package com.complaintandfeedback.nlp;

import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Pipeline {

	private static Properties properties;

	private static String propertiesName = "tokenize, ssplit, pos, lemma, parse, sentiment";

	private static StanfordCoreNLP stanfordCoreNLP;

	public Pipeline() {

	}

	public static StanfordCoreNLP getPipeline() {

		if (stanfordCoreNLP == null) {
			stanfordCoreNLP = new StanfordCoreNLP(properties);
		}

		return stanfordCoreNLP;
	}

	static {
		properties = new Properties();
		properties.setProperty("annotators", propertiesName);
	}

}
