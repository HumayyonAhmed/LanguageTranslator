package com.languagetranslator.translate

import HistoryData
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.languagetranslator.translate.databinding.CameraFragmentBinding
import com.languagetranslator.translate.databinding.FragmentSavedPhrasesBinding

class HistoryFragment : Fragment() {
    private var _binding: FragmentSavedPhrasesBinding? = null
    private val binding get() = _binding!!
    private var listener: DataListener? = null
    private lateinit var historyAdapter: FirebaseRecyclerAdapter<HistoryData, HistoryViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavedPhrasesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DataListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DataListener")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        binding.historyView.visibility = View.GONE

        if(arguments?.getBoolean(HistoryFragment.Favourites.toString()) == true)
        {
            activity?.findViewById<TextView>(R.id.title)?.text = "Favourites"
        }
        else{
            activity?.findViewById<TextView>(R.id.title)?.text = "History"
        }
        // Get a reference to the "history" node
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val googleId = account?.id
        val database = FirebaseDatabase.getInstance("https://languagetranslator-a722c-default-rtdb.firebaseio.com/")
        val historyRef = database.getReference("users/${googleId}/history")

        // Set up options for the FirebaseRecyclerAdapter
        var query: Query = historyRef.orderByChild("dateCreated")

        if(arguments?.getBoolean(HistoryFragment.Favourites.toString()) == true)
        {
            query = historyRef.orderByChild("favourite").equalTo(true)
        }
        else{
            query = historyRef.orderByChild("favourite").equalTo(false)
        }
        val options = FirebaseRecyclerOptions.Builder<HistoryData>()
            .setQuery(query, HistoryData::class.java)
            .build()

        // Set up the RecyclerView and the FirebaseRecyclerAdapter
        historyAdapter = object : FirebaseRecyclerAdapter<HistoryData, HistoryViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_item, parent, false)
                return HistoryViewHolder(view)
            }

            override fun onBindViewHolder(holder: HistoryViewHolder, position: Int, model: HistoryData) {
                holder.sourceTextView.text = model.sourceTxt
                holder.translatedTextView.text = model.translatedTxt
                holder.sourceLang.text = model.sourceLang
                holder.targetLang.text = model.targetLang
                holder.deleteButton.setOnClickListener {
                    // Delete the entire node at the current adapter position
                    getRef(position).removeValue()
                }
                holder.cardView.setOnClickListener{
                    listener?.onDataReceived(model.sourceTxt, model.sourceLang, model.targetLang)

                }
            }
        }
        binding.historyView.adapter = historyAdapter
        binding.historyView.layoutManager = LinearLayoutManager(requireContext())

        binding.progressBar.visibility = View.GONE
        binding.historyView.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        historyAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        historyAdapter.stopListening()
    }

    companion object {

        const val Favourites = false
        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }
}
class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val sourceTextView: TextView = itemView.findViewById(R.id.source_text_view)
    val translatedTextView: TextView = itemView.findViewById(R.id.translated_text_view)
    val sourceLang: TextView = itemView.findViewById(R.id.source_lang_tag_view)
    val targetLang: TextView = itemView.findViewById(R.id.target_lang_tag_view)
    val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    val cardView: CardView = itemView.findViewById(R.id.cardView)
}