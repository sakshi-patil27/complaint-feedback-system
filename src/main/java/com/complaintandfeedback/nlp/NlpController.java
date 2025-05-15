package com.complaintandfeedback.nlp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;

@RestController
@RequestMapping("/nlp")
public class NlpController {
	
//	@Autowired
//	TokenizerExample tokenizerExample;
//	
//	@Autowired
//	SentenceRecognzer sentenceRecognzer;
//	
//	@Autowired
//	PosExample posExample;
	
	@Autowired
	private SentimentAnalysis sentimentAnalysis;
//	
//	@GetMapping("/getResponse")
//	public String getResponse() {
//		return tokenizerExample.Tokenizer();
//	}
//	
//	@GetMapping("/getSplitSentences")
//	public String getSplitSentences() {
//		return sentenceRecognzer.sentenceSplitter();
//	}
//	
//	@GetMapping("/pos")
//	public String partsOfSpeech() {
//		return posExample.partsOfSpeech();
//	}
	
	@PostMapping("/sentiment")
	public String getSentiment(@RequestBody CommonRequestModel body) {
		return sentimentAnalysis.getSentiment(body.getId());
	}
}
