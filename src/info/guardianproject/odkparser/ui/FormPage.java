package info.guardianproject.odkparser.ui;

import java.util.List;
import java.util.Vector;

import org.javarosa.core.model.QuestionDef;

import info.guardianproject.odkparser.Constants;
import info.guardianproject.odkparser.FormWrapper;
import info.guardianproject.odkparser.R;
import info.guardianproject.odkparser.Constants.Form;
import info.guardianproject.odkparser.Constants.Logger;
import info.guardianproject.odkparser.Constants.Form.Keys;
import info.guardianproject.odkparser.FormWrapper.UIBinder;
import info.guardianproject.odkparser.R.layout;
import info.guardianproject.odkparser.ui.FormWidgetFactory.ODKView;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FormPage extends Fragment implements Constants {
	private static final String LOG = Logger.UI;
	
	Activity a;
	private int bind_id;
	View form_page_root;
	List<ODKView> questions;
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		Log.d(LOG, "createview called");
		if(container == null)
			return null;
		
		if(getArguments().containsKey(Form.Keys.BIND_ID))
			bind_id = getArguments().getInt(Form.Keys.BIND_ID);
		else
			return null;
		
		View top_root = li.inflate(R.layout.form_page_fragment, container, false);
		form_page_root = top_root.findViewById(R.id.form_page_root);
		
		questions = ((UIBinder) a).getQuestionsForDisplay(bind_id, bind_id + Form.MAX_QUESTIONS_PER_PAGE);
		return top_root;
	}
	
	public void initLayout() {
		for(ODKView qd : questions) {
			try {
				try {
					// XXX: this fixes dynamic layout bugs...
					((LinearLayout) qd.view.getParent()).removeView(qd.view);
				} catch(NullPointerException e) {}
				
				((LinearLayout) form_page_root).addView(qd.view);
				if(qd.hasInitialValue)
					qd.populateAnswer();
				
			} catch(NullPointerException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			} catch(IllegalStateException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;		
	}
	
	@Override
	public void onResume() {
		initLayout();
		super.onResume();
	}

	public void getAnswers() {
		for(ODKView odkView : questions) {
			if(odkView.setAnswer()) {
				if(((UIBinder) a).answerQuestion(odkView)) {
					Log.d(LOG, "answered the question!");
				}
			} else {
				// answer not set, should be skipped
			}
		}
	}
}
