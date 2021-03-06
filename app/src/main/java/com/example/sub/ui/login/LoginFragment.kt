package com.example.sub.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sub.MainActivity
import com.example.sub.ProfileFragment
import com.example.sub.R
import com.example.sub.databinding.FragmentLoginBinding
import kotlinx.coroutines.runBlocking


class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
//    val model = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        loginViewModel = (activity as LoginActivity?)!!.getLoginViewModel()

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading
        val toRegistrationButton = binding.toRegistration


        // Checks if the typed email and password follow the defined format in LoginViewModel.
        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid  // Disables the login button if the email and password has incorrect format.
                loginFormState.emailError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        // Calls updateUiWithUser (opens MainActivity) if the login succeeded from a database
        // standpoint. If an error occurs, an error message is shown on the screen.
        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(it, view)
                }
            })

        // The listener checks the email and password format while the text is typed.
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }

        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)

        // Start the login process when the "Logg in" button on the keyboard is pressed.
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            runBlocking {
                loadingProgressBar.visibility = View.VISIBLE
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        // Start the logg in process when the "Logg in" button on the screen (fragment) is pressed.
        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            runBlocking {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }

        toRegistrationButton.setOnClickListener {
            navController!!.navigate(R.id.action_loginFragment2_to_registrationFragment2)
            Log.d("myDebug", "To registration")
        }
    }

    /**
     * Calls startMainActivity() from LoginActivity.
     * <p>
     * This function is called when the login succeeded and a new activity is supposed to start
     */
    private fun updateUiWithUser(model: LoggedInUserView, view: View) {
//        val welcome = getString(R.string.welcome)
//        val appContext = context?.applicationContext ?: return
//        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
        (activity as LoginActivity?)!!.startMainActivity()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}