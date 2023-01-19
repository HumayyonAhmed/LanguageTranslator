package com.languagetranslator.translate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/***
 * Fragment view for handling translations
 */
class CommunicateFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.communicate_fragment, container, false)
    }

    companion object {
        fun newInstance(): CommunicateFragment {
            return CommunicateFragment()
        }
    }
}