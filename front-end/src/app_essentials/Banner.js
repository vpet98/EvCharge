import React from 'react';
import './Banner.css';
import { pages } from './App.js';
import { postLogout } from '../api_comm/api.js';
import M from 'materialize-css';
import AppiErrorHandler from '../api_comm/error_handling.js';

// A banner component
class Banner extends React.Component{
  constructor(props){
    super(props);
    this.handleHome = this.handleHome.bind(this);
    this.handleLogin = this.handleLogin.bind(this);
    this.handleLogout = this.handleLogout.bind(this);
  }

  componentDidMount() {
    let elems = document.querySelectorAll('.dropdown-trigger');
    M.Dropdown.init(elems, {inDuration: 300, outDuration: 225, coverTrigger: false});
  }

  // handle click "go to login page" button
  handleLogin(e){
    this.props.callback({
      page: pages.login,
      user: null
    });
  }

  // handle logout
  // If everything OK then change the page of the App and remove user
  // else alert user
  handleLogout(e){
    if(this.props.user !== null){
      let reqObj = this.props.user.token;
      postLogout(reqObj)
        .then(
          json => {
            setTimeout(() => {
              this.props.callback({
                page: pages.main,
                user: null
              })
            }, 0)
          })
          .catch(err => {
            let handler = new AppiErrorHandler(err);
            alert("Got a problem while trying to logout user " + this.props.user.username + ":\n" + handler.getError());
          })
          .finally(() => {
            localStorage.removeItem('user');
          });
    }
  }

  handleHome(e){
    this.props.callback({
      page: pages.main
    });
  }

  render(){
    let userName = this.props.user ? this.props.user.username : "<anonymous>";
    return(
      <div className="banner">
          <h3 onClick={this.handleHome} >
          <img src="../logo192.png" alt="Logo" onClick={this.handleHome}/>EvCharge</h3>
        {this.props.user !== null &&(
          <div className="user_menu">
            <a
              type="button"
              className="btn waves-effect waves-light dropdown-trigger"
              id="dropdowner"
              href='#!'
              data-target='dropdown1'
              name="user_btn"
            >
              {userName}
            </a>
            <ul id='dropdown1' className='dropdown-content'>
              <li><a href="#!" onClick={this.handleLogout}>Logout</a></li>
            </ul>
          </div>
        )}
        {this.props.user === null &&(
          <button
            type="button"
            className="btn waves-effect waves-light login_btn"
            name="login"
            onClick={this.handleLogin}
          >
            Login
          </button>
        )}
        <button
          type="button"
          className="btn waves-effect waves-light home_btn"
          name="home"
          onClick={this.handleHome}
        >
          Home
        </button>
      </div>
    );
  }
}

export default Banner;
