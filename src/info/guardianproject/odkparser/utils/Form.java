package info.guardianproject.odkparser.utils;

import info.guardianproject.odkparser.FormWrapper;
import info.guardianproject.odkparser.utils.QD;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class Form extends Model {
	public String title = null;
	public String namespace = null;
	public String path = null;

	FormWrapper fw = null;
	Activity a = null;
	
	public Form(Activity a, InputStream is) {
		this.a = a;
		fw = new FormWrapper(is);
	}

	public boolean associate(View answerHolder, String questionId) {
		return associate(null, answerHolder, questionId);
	}

	public boolean associate(String initialValue, View answerHolder, String questionId) {
		QD questionDef = fw.questions.get(fw.questions.indexOf(getQuestionDefByTitleId(questionId)));
		if(questionDef != null) {
			questionDef.pin(initialValue, answerHolder);
			return true;
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public List<View> buildUI(String[] initialValues, int[] inputLayout, int[] selectOneLayout, int[] selectMultiLayout, int[] audioCaptureLayout) {
		LayoutInflater li = LayoutInflater.from(a);
		
		List<View> views = new Vector<View>();

		int v = 0;
		for(QD questionDef : fw.questions) {
			View view = null;
			switch(questionDef.getQuestionDef().getControlType()) {
			case org.javarosa.core.model.Constants.CONTROL_INPUT:
				view = li.inflate(inputLayout[0], null);
				view.setTag(QD.map(inputLayout));
				break;
			case org.javarosa.core.model.Constants.CONTROL_SELECT_ONE:
				view = li.inflate(selectOneLayout[0], null);
				view.setTag(QD.map(selectOneLayout));
				break;
			case org.javarosa.core.model.Constants.CONTROL_SELECT_MULTI:
				view = li.inflate(selectMultiLayout[0], null);
				view.setTag(QD.map(selectMultiLayout));
				break;
			case org.javarosa.core.model.Constants.CONTROL_AUDIO_CAPTURE:
				view = li.inflate(audioCaptureLayout[0], null);
				view.setTag(QD.map(audioCaptureLayout));
				break;
			}

			Map<String, Integer> viewMap = (Map<String, Integer>) view.getTag();
			Log.d(LOG, "ok have a map:");
			Iterator<Entry<String, Integer>> vIt = viewMap.entrySet().iterator();
			while(vIt.hasNext()) {
				Entry<String, Integer> entry = vIt.next();
				Log.d(LOG, entry.getKey() + ": " + entry.getValue());
			}

			view = questionDef.buildUI(a, view, initialValues != null ? initialValues[v] : null);
			if(view != null) {
				questionDef.pin(initialValues != null ? initialValues[v] : null, questionDef.getAnswerHolder());
			}

			view.setId(v);
			views.add(view);
			v++;
		}

		return views;
	}

	public void answer(String questionId) {
		QD questionDef = fw.questions.get(fw.questions.indexOf(getQuestionDefByTitleId(questionId)));
		if(questionDef != null) {
			questionDef.answer();
		}
	}

	public OutputStream save(OutputStream os) {
		for(QD questionDef : fw.questions) {
			questionDef.commit(fw);
		}

		return fw.processFormAsXML(os);
	}

	public QD getQuestionDefByTitleId(String questionId) {
		for(QD qd : fw.questions) {
			Log.d(LOG, "QUESTION DEF ID: " + qd.id);
			if(qd.id.equals(questionId)) {
				return qd;
			}
		}

		return null;
	}
}