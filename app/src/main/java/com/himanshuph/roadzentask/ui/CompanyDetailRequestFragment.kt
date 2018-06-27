package com.himanshuph.roadzentask.ui


import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.himanshuph.roadzentask.Injection

import com.himanshuph.roadzentask.R
import com.himanshuph.roadzentask.data.model.RequestDetails
import com.himanshuph.roadzentask.utils.gone
import com.himanshuph.roadzentask.utils.inflate
import com.himanshuph.roadzentask.utils.rx.AppSchedulerProvider
import com.himanshuph.roadzentask.utils.visible
import kotlinx.android.synthetic.main.fragment_company_detail_request.*
import com.himanshuph.roadzentask.data.model.Question
import com.himanshuph.roadzentask.utils.getString
import org.w3c.dom.Text


class CompanyDetailRequestFragment : Fragment(), CompanyContract.View {

    var mPresenter: CompanyContract.Presenter? = null
    var companyFormHeader : String = ""
    var requesterFormHeader : String = ""
    lateinit var nextBtn: Button
    lateinit var backBtn: Button
    var headerTextView: TextView? = null
    var mCompanyTILInfoList: ArrayList<TextInputInfo> = ArrayList()
    var mRequesterTILInfoList: ArrayList<TextInputInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = CompanyRequestPresenter(Injection.provideAppDataManager(), AppSchedulerProvider())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = container?.inflate(R.layout.fragment_company_detail_request)
        mPresenter?.attachView(this)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.getCompanyViewInfo()
    }

    override fun showRequesterFormView(requestDetails: RequestDetails) {
        requesterFormHeader = requestDetails.header
        updateHeaderTextView(requesterFormHeader)
        mRequesterTILInfoList = ArrayList()
        requestDetails.questions.forEach { question ->
            addEditText(question, mRequesterTILInfoList)
        }

        addBackBtn()
        backBtn.setOnClickListener(backBtnClickListener)
        progressBar.gone()
        errorTv.gone()
        nextBtn.gone()
        updateVisibiltyForCompanyView(View.GONE)
    }

    override fun showCompanyFormView(requestDetails: RequestDetails) {
        companyFormHeader = requestDetails.header
        updateHeaderTextView(companyFormHeader)
        mCompanyTILInfoList = ArrayList()
        requestDetails.questions.forEach { question ->
            addEditText(question, mCompanyTILInfoList)
        }
        addNextBtn()
        nextBtn.setOnClickListener(nextBtnClickListener)
        progressBar.gone()
        errorTv.gone()
    }


    val nextBtnClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            var isValid = true
            mCompanyTILInfoList.forEach { textInputInfo ->
                val hint = textInputInfo.question.hint
                val validaton = textInputInfo.question.validation
                val til = view?.findViewById<TextInputLayout>(textInputInfo.viewId)
                til?.let {
                    val text = it.getString()
                    if (text.isEmpty()) {
                        it.isErrorEnabled = true
                        it.error = "${hint} cannot be empty"
                        isValid = false
                    } else if (validaton != null && text.length != validaton.size) {
                        it.isErrorEnabled = true
                        it.error = "${hint} must be of length ${validaton.size}"
                        isValid = false
                    } else {
                        it.error = null
                        it.isErrorEnabled = false
                    }
                }
            }

            if (isValid) {
                if (mRequesterTILInfoList.isEmpty())
                    mPresenter?.getRequesterViewInfo()
                else {
                    updateHeaderTextView(requesterFormHeader)
                    updateVisibiltyForRequesterView(View.VISIBLE)
                    updateVisibiltyForCompanyView(View.GONE)
                    nextBtn.gone()
                    backBtn.visible()
                }
            }
        }
    }


    val backBtnClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            var isValid = true
            mRequesterTILInfoList.forEach { textInputInfo ->

                val hint = textInputInfo.question.hint
                val validaton = textInputInfo.question.validation
                val til = if (textInputInfo.question.type.equals("image"))
                    null
                else
                    view?.findViewById<TextInputLayout>(textInputInfo.viewId)
                til?.let {
                    val text = it.getString()
                    if (text.isEmpty()) {
                        it.isErrorEnabled = true
                        it.error = "${hint} cannot be empty"
                        isValid = false
                    } else if (validaton != null && text.length != validaton.size) {
                        it.isErrorEnabled = true
                        it.error = "${hint} must be of length ${validaton.size}"
                        isValid = false
                    } else {
                        it.error = null
                        it.isErrorEnabled = false
                    }
                }
            }

            if (isValid) {
                updateHeaderTextView(companyFormHeader)
                updateVisibiltyForRequesterView(View.GONE)
                updateVisibiltyForCompanyView(View.VISIBLE)
                nextBtn.visible()
                backBtn.gone()
            }
        }
    }

    private fun addNextBtn() {
        nextBtn = Button(context)
        nextBtn.text = "Next"
        nextBtn.setBackgroundColor(Color.BLUE)
        nextBtn.setTextColor(Color.WHITE)
        nextBtn.layoutParams = getLayoutParams().apply { setMargins(50, 100, 50, 0) }
        ll.addView(nextBtn)
    }

    private fun addBackBtn() {
        backBtn = Button(context)
        backBtn.text = "Back"
        backBtn.setBackgroundColor(Color.BLUE)
        backBtn.setTextColor(Color.WHITE)
        backBtn.layoutParams = getLayoutParams().apply { setMargins(50, 100, 50, 0) }
        ll.addView(backBtn)
    }


    private fun addEditText(question: Question, tilInfoList: ArrayList<TextInputInfo>) {
        val editTextlp = getLayoutParams()
        val tILP = getLayoutParams().apply { setMargins(50, 100, 50, 0) }
        val textInputLayout = TextInputLayout(context)
        textInputLayout.apply {
            id = View.generateViewId()
            tilInfoList.add(TextInputInfo(id, question))
            hint = question.hint
            layoutParams = tILP
        }

        val editText = EditText(context)
        editText.apply {
            id = View.generateViewId()
            when (question.type) {
                "textNumeric" -> inputType = InputType.TYPE_CLASS_NUMBER
                else -> inputType = InputType.TYPE_CLASS_TEXT
            }
            layoutParams = editTextlp
        }

        textInputLayout.addView(editText, editTextlp)
        ll.addView(textInputLayout, tILP)
    }

    private fun updateHeaderTextView(headerText: String) {
        if (headerTextView != null) {
            headerTextView!!.text = headerText
        } else {
            headerTextView = TextView(context);
            val lp = getLayoutParams()
            lp.setMargins(25, 25, 25, 25);
            headerTextView?.apply {
                text = headerText
                id = View.generateViewId()
                layoutParams = lp
                gravity = Gravity.CENTER;
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f);
            }
            ll.addView(headerTextView)
        }
    }

    fun updateVisibiltyForRequesterView(visibility: Int) {
        mRequesterTILInfoList.forEach { textInputInfo ->
            val til = view?.findViewById<View>(textInputInfo.viewId)
            til?.visibility = visibility
        }
    }

    fun updateVisibiltyForCompanyView(visibility: Int) {
        mCompanyTILInfoList.forEach { textInputInfo ->
            val til = view?.findViewById<View>(textInputInfo.viewId)
            til?.visibility = visibility
        }
    }

    private fun getLayoutParams() = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

    override fun showLoading() {
        progressBar.visible()
        errorTv.gone()
    }

    override fun showError() {
        progressBar.gone()
        errorTv.visible()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.detachView()
    }


    companion object {
        @JvmField
        val TAG = "CompanyFragment"

        @JvmStatic
        fun newInstance(): CompanyDetailRequestFragment {
            val fragment = CompanyDetailRequestFragment()
            return fragment
        }
    }
}
