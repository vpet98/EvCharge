import React from 'react';
import './Footer.css';
/*
<footer class="page-footer">
  <div class="container">
    <div class="row">
      <div class="col l6 s12">
        <p class="grey-text text-lighten-4">Brought to you by killercarrots</p>
      </div>
      <div class="col l4 offset-l2 s12">
        <a class="grey-text text-lighten-3" href="https://github.com/ntua/TL20-46/">github.com/ntua/TL20-46</a>
      </div>
    </div>
  </div>
  <div class="footer-copyright">
    <div class="container">
      Copyright © 2020 ECE NTUA, Greece
      <a class="grey-text text-lighten-4 right" href="#!">More Links</a>
    </div>
  </div>
</footer>
*/
// A footer component
function Footer() {
  return(
  <footer>Brought to you by killercarrots<br/>
    <a href="https://github.com/ntua/TL20-46/">github.com/ntua/TL20-46</a><br/>
    <as class="copyright">Copyright © 2020 ECE NTUA, Greece</as>
  </footer>
  );
}

export default Footer;
