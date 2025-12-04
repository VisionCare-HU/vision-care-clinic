import React, { useState } from 'react';
import { API_BASE_URL } from '../config/api';

export default function LoginModal({ onClose, onLoginSuccess }) {
    const [isRegistering, setIsRegistering] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        const formData = new FormData(e.target);
        const data = {
            email: formData.get('email'),
            password: formData.get('password')
        };

        if (isRegistering) {
            // Add registration-specific fields
            data.firstName = formData.get('firstName');
            data.lastName = formData.get('lastName');
            data.phone = formData.get('phone');
        }

        try {
            const endpoint = isRegistering ? '/api/auth/register' : '/api/auth/login';
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                const result = await response.json();
                // Store token if your backend returns one
                if (result.token) {
                    localStorage.setItem('token', result.token);
                }
                onLoginSuccess();
            } else {
                const errorData = await response.json();
                setError(errorData.message || `Registration failed: ${response.status}`);
            }
        } catch (err) {
            setError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-60 z-50 flex justify-center items-center p-4">
            <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md relative transition-all duration-300">
                <button onClick={onClose} className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 text-2xl">&times;</button>

                <h2 className="text-2xl font-bold text-center text-indigo-600 mb-6">
                    {isRegistering ? 'Create Your Account' : 'Patient Portal Login'}
                </h2>

                {/* Error Message */}
                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error}
                    </div>
                )}

                <form onSubmit={handleFormSubmit} className="space-y-4">
                    {/* These fields only show when isRegistering is true */}
                    {isRegistering && (
                        <>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                                    <input name="firstName" id="firstName" type="text" required className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"/>
                                </div>
                                <div>
                                    <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                                    <input name="lastName" id="lastName" type="text" required className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"/>
                                </div>
                            </div>
                            <div>
                                <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">Phone Number</label>
                                <input name="phone" id="phone" type="tel" required className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"/>
                            </div>
                        </>
                    )}

                    {/* These fields are for both login and registration */}
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
                        <input name="email" id="email" type="email" required className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"/>
                    </div>
                    <div>
                        <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">Password</label>
                        <input name="password" id="password" type="password" required className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"/>
                    </div>

                    <button 
                        type="submit" 
                        disabled={loading}
                        className="w-full bg-indigo-600 text-white font-bold py-3 rounded-lg hover:bg-indigo-700 transition-all disabled:opacity-50"
                    >
                        {loading ? 'Processing...' : (isRegistering ? 'Register' : 'Sign In')}
                    </button>

                    <p className="text-center text-sm text-gray-600 pt-2">
                        {isRegistering ? (
                            <>
                                Already have an account?{' '}
                                <button type="button" onClick={() => setIsRegistering(false)} className="font-medium text-indigo-600 hover:underline">Sign In</button>
                            </>
                        ) : (
                            <>
                                Don't have an account?{' '}
                                <button type="button" onClick={() => setIsRegistering(true)} className="font-medium text-indigo-600 hover:underline">Register here</button>
                            </>
                        )}
                    </p>
                </form>
            </div>
        </div>
    );
}