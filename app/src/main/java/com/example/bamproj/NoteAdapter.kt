import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bamproj.Note
import com.example.bamproj.OnNoteClickListener
import com.example.bamproj.R
import java.text.SimpleDateFormat
import java.util.Locale

class NoteAdapter(
    private val noteList: List<Note>,
    private val onNoteClickListener: OnNoteClickListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = noteList[position]
        holder.bind(currentNote)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewNoteTitle)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewNoteDate)
        private val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)

        init {
            itemView.setOnClickListener {
                onNoteClickListener.onNoteClick(adapterPosition)
            }

            deleteButton.setOnClickListener {
                onNoteClickListener.onNoteDelete(adapterPosition)
            }
        }

        fun bind(note: Note) {
            titleTextView.text = note.title
            // Formatowanie daty wg potrzeb
            dateTextView.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(note.date)
        }
    }
}
