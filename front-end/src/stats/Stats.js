import React from 'react';
import './Stats.css';
import { pages, user_roles } from '../app_essentials/App.js';
import GuestStats from './GuestStats.js';
import AdminStats from './AdminStats.js';
import OperatorStats from './OperatorStats.js';
import UserStats from './UserStats.js';

// the stats page component
class Stats extends React.Component{
  constructor(props){
    super(props);
    this.handleHome = this.handleHome.bind(this);
  }

  // handle click home button
  handleHome(e){
    this.props.callback({
      page: pages.main,
      user: this.props.user
    });
  }

  render(){
    let showStats = this.props.user !== null && this.props.user.hasOwnProperty('roles');
    return(
      <>
        {!showStats &&(
          <GuestStats />
        )}
        {showStats && this.props.user.roles.includes(user_roles.admin) &&(
          <AdminStats />
        )}
        {showStats && this.props.user.roles.includes(user_roles.operator) &&(
          <OperatorStats user={this.props.user}/>
        )}
        {showStats && this.props.user.roles.includes(user_roles.user) &&(
          <UserStats user={this.props.user}/>
        )}
      </>
    );
  }
}

export default Stats;
