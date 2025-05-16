package com.complaintandfeedback.nlp;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

@Service
public class SentimentAnalysis {
	
	public String getSentiment(String text) {
		
		StanfordCoreNLP stanfordCoreNLP = Pipeline.getPipeline();
		
//		String text = body.getText();
		
		CoreDocument coreDocument = new CoreDocument(text);
		
		stanfordCoreNLP.annotate(coreDocument);
		
		int totalScore = 0;
        int sentenceCount = 0;
        String sentiment = null;
		
		List<CoreSentence> sentences = coreDocument.sentences();
		
		for(CoreSentence sentence :sentences) {
			sentiment = sentence.sentiment();
			int score = mapSentimentToScore(sentiment);
			System.out.println(sentence.toString() + " " + sentiment + " " + score);
			totalScore += score;
            sentenceCount++;
		}
		
		double averageScore = (double) totalScore / sentenceCount;
        String overallSentiment = mapScoreToSentiment(averageScore);
		System.out.println("overallSentiment " + overallSentiment );
		
		return overallSentiment;
	}
	
	private static int mapSentimentToScore(String sentiment) {
        switch (sentiment.toLowerCase()) {
            case "very negative": return 0;
            case "negative": return 1;
            case "neutral": return 2;
            case "positive": return 3;
            case "very positive": return 4;
            default: return 2; // fallback to neutral
        }
    }
	
	private static String mapScoreToSentiment(double score) {
        if (score < 0.5) return "Very negative";
        else if (score < 1.5) return "Negative";
        else if (score < 2.5) return "Neutral";
        else if (score < 3.5) return "Positive";
        else return "Very positive";
    }
}
