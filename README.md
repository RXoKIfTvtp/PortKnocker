# PortKnocker
Used for hiding open ports on a server from the public

## Features

- Cross-platform (Java 6 or newer).
- Uses RSA Keys for authentication.
- Hides open ports.

## Client Arguments

[-rh|-rhost]        The remote host to send a knock to.<br/>
[-rp|-rport]        The port on the rhost.<br/>
[-lh|-lhost]        The host the rhost should connect back to.<br/>
[-lp|-lport]        The port on the lhost.<br/>
[-k|-key]           The key to use for the knock.<br/>
[-e|-execute]       The command to execute on the rhost.<br/>
[-g|-gen|-generate] The amount of bits to use for the generate RSA keys.<br/>
[-o|-out|-output]   The output directory for the generate RSA keys.<br/>

## Server Arguments

[-u|-unsecure]      Disables security and authentication.<br/>
[-k|-key|-keys]     Specifies a key file or folder for auth.<br/>
[-p|-port]          Specifies the port to listen for knocks on.<br/>

## How to Use

### 1.
Generate an RSA Key pair for authentication<br/>
`$ java -jar knocker-client.jar -g`<br/>
or<br/>
`$ java -jar knocker-client.jar -g 4096`<br/>

### 2.
Copy the public key to any machine that will be accessed remotely.

### 3.
On each remote machine that will be accessed start the knocker-server with the path to the public key as an argument<br/>
`$ java -jar knocker-server.jar -p 5555 -k public-key.pub`

### 4.
Request a remote machine to do something.

Modify the hosts file to allow a specific IP address to connect to sshd:<br/>
`$ java -jar knocker-client.jar -k private-key.pri -rhost 127.0.0.1 -rport 5555 -e "echo -e -n '\nsshd: ALL EXCEPT  192.168.0.100' >> /etc/hosts.deny"`

Reverse shell:<br/>
`$ java -jar knocker-client.jar -k private-key.pri -rhost 127.0.0.1 -rport 5555 -lhost 127.0.0.1 -lport 4444`

## Notes
This port knocker runs on UDP and does not respond using UDP. If the server receives a valid request matching the key it was supplied as an argument it will execute the command or initiate a reverse connection without a UDP response.


