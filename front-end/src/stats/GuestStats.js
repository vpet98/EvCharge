import React from 'react';

// the stats page for guests component
class GuestStats extends React.Component {
  render(){
    return(
      <>
        <h5>Guest Stats</h5>
        <p>{new Date().toString()}</p>
        <p>To access the ev or stations Stats you need to login first</p>
      </>
    );
  }
}

export default GuestStats;
