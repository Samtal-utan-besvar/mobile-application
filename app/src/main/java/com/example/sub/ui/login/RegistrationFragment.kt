package com.example.sub.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.sub.databinding.FragmentRegistrationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegistrationFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        loginViewModel = (activity as LoginActivity?)!!.getLoginViewModel()

        val usernameEditText = binding.regEmail
        val passwordEditText = binding.password
        val registerButton = binding.register
        val loadingProgressBar = binding.loading
        val toRegistrationButton = binding.toLogin

        // Registration
        val phoneEditText = binding.phone
        //val regpasswordEditText = binding.password
        val cpasswordEditText = binding.cpassword
        val nameEditText = binding.regFirstname
        val surnameEditText = binding.regSurname
        val emailEditText = binding.regEmail


        // Checks if the typed email and password follow the defined format in LoginViewModel.
        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                registerButton.isEnabled = loginFormState.isDataValid // Disables the login button if the email and password have incorrect format.
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
                loginFormState.cpasswordError?.let {
                    cpasswordEditText.error = getString(it)
                }
                loginFormState.emailError?.let {
                    usernameEditText.error = getString(it)
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

        // The listener checks the email and password format *while* the text is typed.
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.registrationDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString(),
                    cpasswordEditText.text.toString()
                )
            }
        }

        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        cpasswordEditText.addTextChangedListener(afterTextChangedListener)

        // Start the login process when the "Registration" button on the keyboard is pressed.
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            //  TODO: Never passes the if-statement, fix this or remove this action listener.
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        loginViewModel.register(
                            phoneEditText.text.toString(),
                            passwordEditText.text.toString(),
                            cpasswordEditText.text.toString(),
                            nameEditText.text.toString(),
                            surnameEditText.text.toString(),
                            emailEditText.text.toString()
                        )
                    }
                }
            }
            false
        }

        // Start the registration process when the "Register" button on the screen (fragment) is pressed.
        registerButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    loginViewModel.register(
                        phoneEditText.text.toString(),
                        passwordEditText.text.toString(),
                        cpasswordEditText.text.toString(),
                        nameEditText.text.toString(),
                        surnameEditText.text.toString(),
                        emailEditText.text.toString()
                    )
                }
            }
        }

        toRegistrationButton.setOnClickListener {
            navController!!.navigate(R.id.action_registrationFragment2_to_loginFragment2)
        }


    }

    /**
     * Calls startMainActivity() from LoginActivity.
     * <p>
     * This function is called when the register succeeded and a new activity is supposed to start
     */
    private fun updateUiWithUser(model: LoggedInUserView, view: View) {
        val welcome = getString(R.string.welcome) + model.displayName
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