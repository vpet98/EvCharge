import React from 'react';
import './Login.css';

class Login extends React.Component{
  constructor(props){
    super(props);
  }


  render(){
    return(
      <div className="login_page">
        <h4>EvCharge</h4>
        <h4>Login and start Charging</h4>
          <div className="login_div">
            <p>UserName</p>
            <input
              type="text"
              name="username"
              field="username"
              placeholder="username"
              value=""
            />
            <p>Password</p>
            <input
              type="text"
              name="password"
              field="password"
              placeholder="password"
              value=""
            />
            <button
              type="button"
              name="login"
            >
              Login
            </button>
          </div>
      </div>
    );
  }
}

export default Login;
