/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;

import com.sshtools.j2ssh.io.ByteArrayWriter;

import com.sshtools.j2ssh.transport.cipher.SshCipher;

import com.sshtools.j2ssh.transport.compression.SshCompression;

import com.sshtools.j2ssh.transport.hmac.SshHmac;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;

import java.math.BigInteger;

import java.util.Random;


class TransportProtocolOutputStream {
    //implements Runnable {
    private static Log log = LogFactory.getLog(TransportProtocolOutputStream.class);
    private OutputStream out;

    //private Socket socket;
    private TransportProtocolAlgorithmSync algorithms;
    private TransportProtocolCommon transport;
    private long sequenceNo = 0;
    private long sequenceWrapLimit = BigInteger.valueOf(2).pow(32).longValue();
    private Random rnd = ConfigurationLoader.getRND();
    private long bytesTransfered = 0;

    /**
     * Creates a new TransportProtocolOutputStream object.
     *
     * @param out DOCUMENT ME!
     * @param transport DOCUMENT ME!
     * @param algorithms DOCUMENT ME!
     *
     * @throws TransportProtocolException DOCUMENT ME!
     */
    public TransportProtocolOutputStream( /*Socket socket,*/
        OutputStream out, TransportProtocolCommon transport,
        TransportProtocolAlgorithmSync algorithms)
        throws TransportProtocolException {
        // try {
        //this.socket = socket;
        this.out = out; //socket.getOutputStream();
        this.transport = transport;
        this.algorithms = algorithms;

        /* } catch (IOException ioe) {
              throw new TransportProtocolException(
         "Failed to obtain socket output stream");
          }*/
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected long getNumBytesTransfered() {
        return bytesTransfered;
    }

    /**
     * DOCUMENT ME!
     *
     * @param msg DOCUMENT ME!
     *
     * @throws TransportProtocolException DOCUMENT ME!
     */
    protected synchronized void sendMessage(SshMessage msg)
        throws TransportProtocolException {
        try {
            // Get the algorithm objects
            algorithms.lock();

            SshCipher cipher = algorithms.getCipher();
            SshHmac hmac = algorithms.getHmac();
            SshCompression compression = algorithms.getCompression();

            // Get the message payload data
            byte[] msgdata = msg.toByteArray();
            int padding = 4;
            int cipherlen = 8;

            // Determine the cipher length
            if (cipher != null) {
                cipherlen = cipher.getBlockSize();
            }

            // Determine the padding length
            padding += ((cipherlen
            - ((msgdata.length + 5 + padding) % cipherlen)) % cipherlen);

            // Write the data into a byte array
            ByteArrayWriter message = new ByteArrayWriter();

            // Write the packet length field
            message.writeInt(msgdata.length + 1 + padding);

            // Write the padding length
            message.write(padding);

            // Write the message payload
            message.write(msgdata);

            // Create some random data for the padding
            byte[] pad = new byte[padding];
            rnd.nextBytes(pad);

            // Write the padding
            message.write(pad);

            // Get the unencrypted packet data
            byte[] packet = message.toByteArray();
            byte[] mac = null;

            // Generate the MAC
            if (hmac != null) {
                mac = hmac.generate(sequenceNo, packet, 0, packet.length);
            }

            // Do some compression
            if (compression != null) {
                packet = compression.compress(packet);
            }

            // Perfrom encrpytion
            if (cipher != null) {
                packet = cipher.transform(packet);
            }

            // Reset the message
            message.reset();

            // Write the packet data
            message.write(packet);

            // Combine the packet and MAC
            if (mac != null) {
                message.write(mac);
            }

            bytesTransfered += message.size();

            // Send!
            //if (socket.isConnected()) {
            out.write(message.toByteArray());

            //}
            out.flush();

            // Increment the sequence no
            if (sequenceNo < sequenceWrapLimit) {
                sequenceNo++;
            } else {
                sequenceNo = 0;
            }
        } catch (IOException ioe) {
            if (transport.getState().getValue() != TransportProtocolState.DISCONNECTED) {
                throw new TransportProtocolException("IO Error on socket: "
                    + ioe.getMessage());
            }
        }
        finally {
    	    algorithms.release();
        }
    }
}
