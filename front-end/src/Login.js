import React from 'react';
import './Login.css';
import {pages} from './App.js';
import {postLoginToken} from './api.js';

// the login page component
class Login extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      username: "",
      password: "",
      error: null,
      token: null,
      appCallback: props.callback
    };
    this.handleInput = this.handleInput.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleReturn = this.handleReturn.bind(this);
  }

  // handle any change in input areas
  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    this.setState({ [name]: value });
  }

  // handle the submit button of the form
  // If everything OK then change the page of the App and return user
  // else return error
  handleSubmit(e){
    this.setState({ error: null });
    if(this.state.username === ""){
      this.setState({ error: "You have to fill in your username" });
    }else if(this.state.password === ""){
      this.setState({ error: "You have to fill in your password" });
    }else{
      let reqObj = {
        username: this.state.username,
        password: this.state.password
      };
      postLoginToken(reqObj)
        .then(json => {
          setTimeout(() => {
            this.setState({ token: json.data.token });
            let finalUser = {
              username: this.state.username,
              password: this.state.password, // maybe this is not a good IDEA
              token: this.state.token
            };
            this.state.appCallback({
              page: pages.main,
              user: finalUser
            });
          }, 0)
        })
        .catch(err => {
          this.setState({ error: err.message });
        });
    }
  }

  // handle the return button of the form to return to the main page without loging in
  handleReturn(e){
    this.setState({ error: null });
    this.state.appCallback({
      page: pages.main,
      user: null
    });
  }

  render(){
    return(
      <div className="login_page">
        <h4>EvCharge</h4>
        <h4>Login and start Charging</h4>
          <div className="login_div">
            <p>Username</p>
            <input
              type="text"
              name="username"
              field="username"
              placeholder="username"
              value={this.state.username}
              onChange={this.handleInput}
            />
            <p>Password</p>
            <input
              type="text"
              name="password"
              field="password"
              placeholder="password"
              value={this.state.password}
              onChange={this.handleInput}
            />
            <button
              type="button"
              name="login"
              onClick={this.handleSubmit}
            >
              Login
            </button>
            <button
              type="button"
              name="return"
              onClick={this.handleReturn}
            >
              Continue as guest
            </button>
            <p>{ this.state.error }</p>
          </div>
      </div>
    );
  }
}

export default Login;
