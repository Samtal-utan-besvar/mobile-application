package com.example.sub.ui.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sub.R
import com.example.sub.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val AUTOLOGIN_DISABLED = true     // For debugging purposes

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        loginViewModel = ViewModelProvider(this,
            LoginViewModelFactory(context))[LoginViewModel::class.java]

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading
        val toRegistrationButton = binding.toRegistration

        // Removes loggedInUser object from SharedPreferences so that not loggedInUser can't be used for autologin.
        if (AUTOLOGIN_DISABLED) {
            val sharedPref = context?.getSharedPreferences("UserSharedPref", Context.MODE_PRIVATE)
            sharedPref!!.edit().clear().apply()
        }

        if (loginViewModel.isLoggedIn()) {
            (activity as LoginActivity?)!!.startMainActivity()  // Starts MainActivity if loggedInUser is found.
        }

        // Checks if the typed email and password follows the defined format (formats defined in LoginViewModel).
        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid  // Disables the login button if the email and password has incorrect format.
                loginFormState.phoneNumberError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        // Calls updateUiWithUser (opens MainActivity) if the login succeeded from a database
        // standpoint. If not, an error message is shown on the screen.
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

        // Start the logg in process when the "Logg in" button on keyboard is pressed.
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            //  TODO: Never passes the if-statement, fix this.
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        loginViewModel.login(
                            usernameEditText.text.toString(),
                            passwordEditText.text.toString()
                        )
                    }
                }
            }
            false
        }

        // Start the logg in process when the "Logg in" button on the screen (fragment) is pressed.
        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    loginViewModel.login(
                        usernameEditText.text.toString(),
                        passwordEditText.text.toString()
                    )
                }
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
     * This function is called when the a login succeeded and a new activity is supposed to start
     */
    private fun updateUiWithUser(model: LoggedInUserView, view: View) {
        val welcome = getString(R.string.welcome)
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
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