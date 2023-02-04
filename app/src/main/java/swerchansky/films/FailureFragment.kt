package swerchansky.films

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment

class FailureFragment : Fragment() {
   private lateinit var failureFragmentButton: AppCompatButton


   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View? {
      return inflater.inflate(R.layout.failure_fragment, container, false)
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      failureFragmentButton = view.findViewById(R.id.restartButton)

      failureFragmentButton.setOnClickListener {
         (activity as MainActivity).restart()
      }

   }
}
